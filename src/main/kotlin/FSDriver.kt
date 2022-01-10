import errors.SystemError
import kotlin.math.abs
import kotlin.math.pow
import kotlin.text.StringBuilder

class FSDriver {
    val sizeOfBlock = 100
    private val device = Device(sizeOfBlock)
    private var numOfBlocks: Int? = null
    private val maxSizeOfDescriptor = 67 //calculated earlier
    private val blocksForDescriptor = maxSizeOfDescriptor/sizeOfBlock + 1 // use it in case if size of block smaller than descriptor
    private var blocksForBitMap: Int? = null
    private var numOfDescriptors = 0

    fun makeFS(num: Int) {
        numOfDescriptors = num
        device.clearFS()
    }

    fun connectToFS() {
        val size = device.connect()
        numOfBlocks = size/sizeOfBlock
        blocksForBitMap = ((numOfBlocks!!.toDouble()/8)/ numOfBlocks!!).toInt() + 1
    }

    fun deleteAllInfoAboutDevice() {
        // clear all info about device
        //simplified here
        numOfBlocks = null
        blocksForBitMap = null
    }

    fun getDescriptor(id: Int): Descriptor {
        val descriptorStartBit = calculateBlockOfDescriptor(id)*8
        return Descriptor(device.getChartArray(descriptorStartBit).toString())
    }
    
    
    private fun calculateBlockOfDescriptor(id: Int): Int {
        if (blocksForBitMap == null) throw SystemError("no mounted system found")
        return blocksForBitMap!! + id
    }

    fun readDirectoryData(): String {
        return parseDirInfo(device.getCharArrayForList(getDescriptor(0).getAllDataBlocks(this)))
    }

    private fun parseDirInfo(data: List<Byte>): String {
        val dirInfo = StringBuilder()
        for (line in data.toString().split("\n")){
            val splitLine = line.split(" ")
            dirInfo.append(splitLine[0] + getDescriptor(splitLine[1].toInt()).getShortInfo() + "\n")
        }
        return dirInfo.toString()
    }

    fun readBlock(blockLink: Int): String {
        return device.getChartArray(blockLink).toString()
    }

    fun readBlockToArray(blockLink: Int): List<Byte> {
        return device.getChartArray(blockLink)
    }

    fun createDescriptor(type: FileType): Int {
        val freeIndex = findNextFreeBlock(blocksForBitMap!!+1)
        if ((freeIndex- blocksForBitMap!!) > numOfDescriptors) throw SystemError("Max num of descriptors achieved")
        writeBlock(freeIndex, Descriptor(type, 0).toString().toByteArray())
        return freeIndex
    }

    fun writeBlock(index: Int, block: ByteArray) {
        setInBlockMap(index)
        device.writeBlock(index, block)
    }

    private fun setInBlockMap(index: Int) {
        val blockIndex = (index/8)/sizeOfBlock
        val block = device.getChartArray(blockIndex) as ArrayList
        val byteIndex = index/8 - sizeOfBlock*blockIndex
        val bitIndexInByte = index - (index/8)*8
        val base = 2.0
        block[byteIndex] = (block[byteIndex].toInt() + base.pow(bitIndexInByte).toInt()).toByte()
        device.writeBlock(blockIndex, block.toByteArray())
    }

    private fun resetInBlockMap(index: Int) {
        val blockIndex = (index/8)/sizeOfBlock
        val block = device.getChartArray(blockIndex) as ArrayList
        val byteIndex = index/8 - sizeOfBlock*blockIndex
        val bitIndexInByte = index - (index/8)*8
        val base = 2.0
        block[byteIndex] = (block[byteIndex].toInt() - base.pow(bitIndexInByte).toInt()).toByte()
        device.writeBlock(blockIndex, block.toByteArray())
    }

    private fun findNextFreeBlock(startIndex: Int): Int {
        val bitMap = getBitMap()

        for (bit in startIndex until  bitMap.length){
            if (bitMap[bit] == '0'){
                return bit
            }
        }
        throw SystemError("No more free blocks")
    }

    private fun getBitMap(): String {
        val bitMap = StringBuilder()
        for (i in 0..blocksForBitMap!!){
            for(byte in device.getChartArray(i)){
                bitMap.append(byteToBitLine(byte.toInt()))
            }

        }
        return bitMap.toString()
    }

    private fun byteToBitLine(byte: Int): String {
        val bits = StringBuilder()
        for (i in 7 downTo 0){
            bits.append(byte shr i and 1)
        }
        return bits.toString()
    }

    fun addLink(name: String, descriptorIndex: Int) {
        getDescriptor(0).writeToTheEnd("$name $descriptorIndex", this)
    }

    fun compareWithBlockSize(size: Int): Boolean {
        return size <= sizeOfBlock
    }

    fun getFreeBlock(): Int {
        return findNextFreeBlock(blocksForBitMap!! + numOfDescriptors)//in case if descriptor size < block size
    }

    fun createFileDescriptor(nameLink: String): Int {
        val index = getFreeBlock()
        val descriptorId = findDescriptorByName(nameLink)
        val descriptor = getDescriptor(descriptorId)
        descriptor.addLinks()
        writeBlock(calculateBlockOfDescriptor(descriptorId), descriptor.toString().toByteArray() )
        writeBlock(index, FileDescriptor(descriptor.getAllDataBlocks(this), descriptorId).toString().toByteArray())
        return index
    }

    private fun findDescriptorByName(nameLink: String): Int {
        val allLinks = device.getCharArrayForList(getDescriptor(0).getAllDataBlocks(this))
        for(line in allLinks.toString().split("\n")){
            val splitLine = line.split(" ")
            if (splitLine[0] == nameLink) return splitLine[1].toInt()
        }
        throw SystemError("No such file link founded")
    }

    fun deleteFileDescriptor(fdId: Int) {
        val descriptorId = getFileDescriptor(fdId).descriptorId
        val descriptor = getDescriptor(descriptorId)
        if (!descriptor.decreaseLink()){
            clearDescriptor(descriptor, descriptorId)
        }else{
            writeBlock(calculateBlockOfDescriptor(descriptorId), descriptor.toString().toByteArray())
        }
        clearBlock(fdId)
    }

    private fun clearDescriptor(descriptor: Descriptor, descriptorId: Int) {
        for(link in descriptor.getAllDataBlocks(this)){
            clearBlock(link)
        }
        clearBlock(calculateBlockOfDescriptor(descriptorId))
    }

    fun getFileDescriptor(fdId: Int): FileDescriptor {
        return FileDescriptor(readBlock(fdId))
    }

    fun clearBlock(index: Int) {
        resetInBlockMap(index)
        writeBlock(index, arrayOf(0).toString().toByteArray())
    }

    fun createNewLink(newLink: String, linkOnFile: String) {

        val descriptorId = findDescriptorByName(linkOnFile)
        addLink(newLink, descriptorId)
        val descriptor = getDescriptor(descriptorId)
        descriptor.addLinks()
        writeDescriptor(descriptorId, descriptor)
    }

    private fun writeDescriptor(descriptorId: Int, descriptor: Descriptor) {
        writeBlock(calculateBlockOfDescriptor(descriptorId), descriptor.toString().toByteArray())
    }

    fun deleteLink(link: String) {
        val descriptorId = findDescriptorByName(link)
        var descriptor = getDescriptor(descriptorId)
        if (!descriptor.decreaseLink()){
            clearDescriptor(descriptor, descriptorId)
        }
        descriptor = getDescriptor(0)
        val allBlocks =  descriptor.getAllDataBlocks(this)
        for (block in allBlocks){
            val data = readBlock(block)
            if (link in data){
                val newData = StringBuilder()
                for (line in data.split("\n")){
                    if (line[0].toString() == link){
                        continue
                    }
                }
                writeBlock(block, newData.toString().toByteArray())
                return
            }
        }
    }

    fun truncateFile(fileLink: String, size: Int) {
        val descriptor = getDescriptor(findDescriptorByName(fileLink))
        val definition = size - descriptor.size
        if (definition > 0){
            for (i in 0..(definition/sizeOfBlock)){
                descriptor.addLinkOnBlock(getFreeBlock(), this)
            }
        }else{
            for (i in 0..(abs(definition)/sizeOfBlock)){
                descriptor.deleteLastBlockLink(this)
            }
        }
    }

}