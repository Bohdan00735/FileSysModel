import command.Command
import command.CommandChecker

class MySystem {

    private val fsDriver = FSDriver()

    fun doCommand(command: Command){
        CommandChecker(this).checkCommand(command)
        return
    }

    fun makeFileSystemWithNumOfDescriptors(num: Int) {
        fsDriver.makeFS(num)
    }

    fun mountFS() {
        fsDriver.connectToFS()
    }

    fun unmountFS() {
        fsDriver.deleteAllInfoAboutDevice()
    }

    fun showDescriptorInfo(id: Int) {
        println(fsDriver.getDescriptor(id).getInfo())
    }

    fun showAllFilesInCurrentFolder() {
        println(fsDriver.readDirectoryData())
    }

    fun createFile(name: String) {
        //change to directory if needed
        fsDriver.addLink(name, fsDriver.createDescriptor(FileType.REGULAR))
    }

    fun openFile(nameLink: String) {
        println(fsDriver.createFileDescriptor(nameLink))
    }

    fun closeFD(fdId: Int) {
        fsDriver.deleteFileDescriptor(fdId)
    }

    fun readFromFD(fdId: Int, offset: Int, size: Int) {
        fsDriver.getFileDescriptor(fdId).read(offset, size, fsDriver)
    }

    fun writeToFD(fdId: Int, offset: Int, size: Int) {
        fsDriver.getFileDescriptor(fdId).write(offset, size, fsDriver)
    }

    fun addLink(newLink: String,linkOnFile: String) {
        fsDriver.createNewLink(newLink, linkOnFile)

    }

    fun removeLink(link: String) {
        fsDriver.deleteLink(link)
    }

    fun truncateFile(fileLink: String, size: Int) {
        fsDriver. truncateFile(fileLink,size)
    }

    fun makeDirectory(path: String) {
        fsDriver.addDirectory(path)
    }


    fun removeDirectory(path: String) {
        fsDriver.deleteDirectory(path)
    }

    fun goToDir(path: String) {
        fsDriver.changeCurrentDirectory(path)
    }

    fun createSymbolicLink(path: String, link: String) {
        fsDriver.addSymbolicLink(path, link)
    }

    fun showCurrentDirectory() {
        println(fsDriver.getCurrentDirectoryPath())
    }
}