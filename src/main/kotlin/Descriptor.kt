import errors.SystemError

open class Descriptor(var fileType: FileType, var size: Int) {
    constructor() : this(FileType.REGULAR, Int.MAX_VALUE)
    constructor(line: String):this(){
        val descIter = line.split("\n").iterator()

        if (descIter.hasNext()){
            val type = descIter.next()
            fileType = if (type == FileType.REGULAR.toString()) FileType.REGULAR
            else FileType.DIRECTORY
        }else return

        if (fileType == FileType.DIRECTORY){
            if (descIter.hasNext()){
                currentPath = descIter.next()
            }else return

            if (descIter.hasNext()){
                rootPath = descIter.next()
            }else return
        }
        if (descIter.hasNext()){
            size = descIter.next().toInt()
        }else return

        if (descIter.hasNext()){
            numOfLinks = descIter.next().toInt()
        }else return

        if (descIter.hasNext()){
            straightLink1 = descIter.next().toInt()
        }else return

        if (descIter.hasNext()){
            straightLink2 = descIter.next().toInt()
        }else return

        if (descIter.hasNext()){
            blockLink = descIter.next().toInt()
        }else return
    }

    constructor(fileType: FileType, size: Int, currentDir: String, rootDirectory: String) : this(fileType, size){
        currentPath = currentDir
        rootPath = rootDirectory
    }
    private var numOfLinks = 1
    var straightLink1:Int? = null
    var straightLink2:Int? = null
    var blockLink:Int? = null
    private var currentPath: String? = null
    private var rootPath:String? = null

    override fun toString(): String {
        if (fileType == FileType.DIRECTORY) {
            return "$fileType \n" +
                    "$currentPath\n" +
                    "$rootPath\n"+
                    "$size \n" +
                    "$numOfLinks \n" +
                    "$straightLink1 \n" +
                    "$straightLink2 \n" +
                    "$blockLink"
        }
        return "$fileType \n" +
                "$size \n" +
                "$numOfLinks \n" +
                "$straightLink1 \n" +
                "$straightLink2 \n" +
                "$blockLink"
    }

    fun getInfo(): String {
        return "fileType : $fileType \n" +
                "file size : $size \n" +
                "num of links : $numOfLinks \n" +
                "link1 : $straightLink1 \n" +
                "link2 : $straightLink2 \n" +
                "link on block with links : $blockLink"
    }

    fun getAllDataBlocks(fsDriver: FSDriver): ArrayList<Int> {
        val blocksLinks = ArrayList<Int>()
        if (straightLink1 != null)
            blocksLinks.add(straightLink1!!)
        if (straightLink2 != null)
            blocksLinks.add(straightLink2!!)
        if (blockLink != null)
            blocksLinks.addAll(getLinksFromBlock(fsDriver))
        return blocksLinks
    }

    private fun getLinksFromBlock(fsDriver: FSDriver): ArrayList<Int> {
        val links = ArrayList<Int>()
        for (link in fsDriver.readBlock(blockLink!!).split("\n")){
            links.add(link.toInt())
        }
        return links
    }

    fun getShortInfo(): String {
        return "type: $fileType; size: $size"
    }

    fun writeToTheEnd(line: String, fsDriver: FSDriver) {
        if (blockLink != null){
            val links = getLinksFromBlock(fsDriver)
            val block = fsDriver.readBlock(links.last())
            val newLine = block + line + "\n"
            if (fsDriver.compareWithBlockSize(newLine.toByteArray().size)){
                fsDriver.writeBlock(links.last(), newLine.toByteArray())
            }else{
                links.add(fsDriver.getFreeBlock())
                if (!fsDriver.compareWithBlockSize(links.toString().toByteArray().size)) throw SystemError("no more links available")
                fsDriver.writeBlock(blockLink!!, links.toString().toByteArray())
            }
            return
        }

        if (straightLink2 != null){
            val block = fsDriver.readBlock(straightLink2!!)
            val newLine = block + line + "\n"
            if (fsDriver.compareWithBlockSize(newLine.toByteArray().size)){
                fsDriver.writeBlock(straightLink2!!, newLine.toByteArray())
            }else{
                blockLink = fsDriver.getFreeBlock()
                val newBlockLink = fsDriver.getFreeBlock()
                fsDriver.writeBlock(blockLink!!, (newBlockLink.toString() + "\n").toByteArray())
                fsDriver.writeBlock(newBlockLink, newLine.toByteArray())
            }
            return
        }

        if (straightLink1 != null){
            val block = fsDriver.readBlock(straightLink1!!)
            val newLine = block + line
            if (fsDriver.compareWithBlockSize(newLine.toByteArray().size)){
                fsDriver.writeBlock(straightLink1!!, newLine.toByteArray())
            }else{
                straightLink2 = fsDriver.getFreeBlock()
                fsDriver.writeBlock(straightLink2!!, newLine.toByteArray())
            }
            return
        }
    }



    fun addLinks() {
        numOfLinks++
    }

    fun decreaseLink(): Boolean {
        numOfLinks--
        if (numOfLinks > 0) return true
        return false
    }

    fun addLinkOnBlock(freeBlock: Int, fsDriver: FSDriver) {
        if (straightLink1 == null){
            straightLink1 = freeBlock
            return
        }
        if (straightLink2 == null){
            straightLink2 = freeBlock
            return
        }

        if (blockLink == null){
            blockLink = fsDriver.getFreeBlock()
            fsDriver.writeBlock(blockLink!!, (freeBlock.toString() + "\n").toByteArray())
            return
        }
        val links = getLinksFromBlock(fsDriver)
        links.add(freeBlock)
        if (!fsDriver.compareWithBlockSize(links.toString().toByteArray().size)) throw SystemError("no more links available")
        fsDriver.writeBlock(blockLink!!, links.toString().toByteArray())
    }

    fun deleteLastBlockLink(fsDriver: FSDriver) {
        if (blockLink != null){
            val links = getLinksFromBlock(fsDriver)
            fsDriver.clearBlock(links.last())
            links.remove(links.size-1)
            if (links.size == 0){
                fsDriver.clearBlock(blockLink!!)
                blockLink = null
                return
            }
            fsDriver.writeBlock(blockLink!!, links.toString().toByteArray())
            return
        }

        if (straightLink2 != null){
            fsDriver.clearBlock(straightLink2!!)
            straightLink2 = null
            return
        }

        if (straightLink1 != null){
            fsDriver.clearBlock(straightLink1!!)
            straightLink1 = null
            return
        }
        throw SystemError("No more links to delete")
    }

    fun getRootPath(): String {
        if (rootPath == null){
            if (currentPath != null) return currentPath!! //that is root of tree
            throw SystemError("not Directory")
        }
        return rootPath!!
    }

    fun isEmpty(): Boolean {
        return straightLink1 == null
    }
}