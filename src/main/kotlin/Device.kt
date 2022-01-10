
import errors.SystemError
import kotlin.collections.ArrayList

class Device(val sizeOfBlock: Int) {
    //simulation of file on device, to work with real file change methods to read and write to file
    private val fileSystemSize = 20000000
    private var filesystemArray = Array<Byte>(fileSystemSize){0}

    fun clearFS() {
        filesystemArray = Array<Byte>(fileSystemSize){0}
    }

    fun connect(): Int {
        return fileSystemSize
    }

    fun getChartArray(start: Int): List<Byte> {
        if (start+sizeOfBlock >= filesystemArray.size) throw SystemError("Block out of bound")
        return filesystemArray.slice(start..start+sizeOfBlock)
    }

    fun getCharArrayForList(allDataBlocks: ArrayList<Int>):List<Byte>{
        val data = ArrayList<Byte>()
        for (link in allDataBlocks){
            data.addAll(getChartArray(link))
        }
        return data
    }

    fun writeBlock(blockIndex: Int, block: ByteArray) {
        if (blockIndex+sizeOfBlock > filesystemArray.size) throw SystemError("Block out of bound")
        for (i in block.indices){
            filesystemArray[blockIndex+i] = block[i]
        }
    }

}