package command

import MySystem
import errors.SyntaxError
import command.Commands.*

class CommandChecker(private val system: MySystem) {

    fun checkCommand(command: Command){
        when (command.commandType) {
            MKFS -> {
                system.makeFileSystemWithNumOfDescriptors(isInt(command.parameter1!!))
            }
            MOUNT -> {
                system.mountFS()
            }
            UNMOUNT -> {
                system.unmountFS()
            }
            FSTAT -> {
                system.showDescriptorInfo(isInt(command.parameter1!!))
            }
            LS -> {
                system.showAllFilesInCurrentFolder()
            }
            CREATE -> {
                system.createFile(command.parameter1!!)
            }
            OPEN -> {
                system.openFile(command.parameter1!!)
            }
            CLOSE -> {
                system.closeFD(isInt(command.parameter1!!))
            }
            READ -> {
                system.readFromFD(isInt(command.parameter1!!),isInt(command.parameter2!!),isInt(command.parameter3!!))
            }
            WRITE -> {
                system.writeToFD(isInt(command.parameter1!!),isInt(command.parameter2!!),isInt(command.parameter3!!))
            }
            LINK -> {
                system.addLink(command.parameter1!!, command.parameter2!!)
            }
            UNLINK -> {
                system.removeLink(command.parameter1!!)
            }
            TRUNC -> {
                system.truncateFile(command.parameter1!!,isInt(command.parameter2!!) )
            }
            MKDIR->{
                system.makeDirectory(command.parameter1!!)
            }
            RMDIR->{
                system.removeDirectory(command.parameter1!!)
            }
            CD->{
                system.goToDir(command.parameter1!!)
            }
            SYMLINK->{
                system.createSymbolicLink(command.parameter1!!, command.parameter2!!)
            }
        }
    }

    private fun isInt(line: String): Int {
        return line.toIntOrNull()
            ?: throw SyntaxError("Num in parameters expected, get more information about command")
    }
}