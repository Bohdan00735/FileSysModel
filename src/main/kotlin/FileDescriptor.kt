import errors.SystemError
import kotlin.math.pow
import kotlin.properties.Delegates

class FileDescriptor() {
    private val allBlockLinks = ArrayList<Int>()
    var descriptorId by Delegates.notNull<Int>()

    constructor(block: String) : this(){

        descriptorId = block.split("\n").first().toInt()
        for (line in 1 until block.split("\n").size){
            allBlockLinks.add(block[line].code)
        }
    }

    constructor(allDataBlocks: ArrayList<Int>, descriptorId: Int): this(){
        this.descriptorId = descriptorId
        allBlockLinks.addAll(allDataBlocks)
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append(descriptorId.toString() + "\n")
        for (i in allBlockLinks){
            result.append(i.toString() + "\n")
        }
        return result.toString()
    }

    fun read(offset: Int, size: Int, fsDriver: FSDriver): String {
        val blockSize = fsDriver.sizeOfBlock
        val startBlock = offset/blockSize
        val endBlock = (offset + size)/blockSize
        if (endBlock >= allBlockLinks.size) throw SystemError("wrong offset/size")
        val data = StringBuilder()
        for(i in startBlock..endBlock){
            data.append(fsDriver.readBlock(allBlockLinks[i]))
        }
        return data.slice(
            (offset - startBlock*blockSize)..blockSize - (offset+size)-endBlock*blockSize).toString()
    }

    fun write(offset: Int, size: Int, fsDriver: FSDriver) {
        //writes 1 in set bounds
        val blockSize = fsDriver.sizeOfBlock
        val startBlock = offset/blockSize
        val endBlock = (offset + size)/blockSize
        if (endBlock >= allBlockLinks.size) throw SystemError("wrong offset/size")
        var blockData = fsDriver.readBlockToArray(allBlockLinks[startBlock]) as ArrayList

        for (i in 0 until (offset - startBlock*blockSize)){
            blockData[i] = 255.toByte()
        }
        fsDriver.writeBlock(allBlockLinks[startBlock], blockData.toByteArray())
        blockData = fsDriver.readBlockToArray(allBlockLinks[endBlock]) as ArrayList
        for (i in 7 downTo blockSize - (offset+size)-endBlock*blockSize){
            blockData[i] = 255.toByte()
        }
        fsDriver.writeBlock(allBlockLinks[endBlock], blockData.toByteArray())

        for (blockIndex in startBlock+1 until endBlock){
            blockData = fsDriver.readBlockToArray(allBlockLinks[blockIndex]) as ArrayList
            for (i in blockData.indices){
                blockData[i] = 255.toByte()
            }
            fsDriver.writeBlock(allBlockLinks[blockIndex], blockData.toByteArray())
        }
    }
}