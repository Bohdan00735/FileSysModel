package command

import errors.SyntaxError

class CommandsParser {
    private val commands = listOf("mkfs", "mount", "unmount", "fstat", "ls", "create", "open", "close", "read",
    "write", "link", "unlink", "truncate")

    fun parseLine(line:String): Command {
        val splitLine = line.split(" ")
        if (splitLine.isEmpty()) throw SyntaxError("Nothing inputted")
        when (val command = splitLine[0]){
            "mkfs"-> {
                if (splitLine.size != 2) throw SyntaxError("Wrong format for command $command \n" +
                        "should be \$mkfs n #where n - num of descriptors")
                return Command(Commands.MKFS, splitLine[1])
            }
            "mount"->{
                if (splitLine.size != 1) throw SyntaxError("Don`t need parameters for command $command")
                return Command(Commands.MOUNT)
            }
            "unmount"->{
                if (splitLine.size != 1) throw SyntaxError("Don`t need parameters for command $command")
                return Command(Commands.UNMOUNT)
            }
            "fstat"->{
                if (splitLine.size != 2) throw SyntaxError("Wrong format for command $command \n" +
                        "should be \$fstat id #where id - id of descriptor")
                return Command(Commands.FSTAT, splitLine[1])
            }
            "ls"->{
                if (splitLine.size != 1) throw SyntaxError("Don`t need any parameters for command $command")
                return Command(Commands.LS)
            }
            "create"->{
                if (splitLine.size != 2) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for name")
                return Command(Commands.CREATE, splitLine[1])
            }
            "open"->{
                if (splitLine.size != 2) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for name")
                return Command(Commands.OPEN, splitLine[1])
            }
            "close"->{
                if (splitLine.size != 2) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for fd")
                return Command(Commands.CLOSE, splitLine[1])
            }
            "read"->{
                if (splitLine.size != 4) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for $command [fd] [offset] [size] \n" +
                        "where: fd - numeric file descriptor \n")
                return Command(Commands.READ, splitLine[1], splitLine[2], splitLine[3])
            }
            "write"->{
                if (splitLine.size != 4) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for $command [fd] [offset] [size] \n" +
                        "where: fd - numeric file descriptor \n")
                return Command(Commands.WRITE, splitLine[1], splitLine[2], splitLine[3])
            }
            "link"->{
                if (splitLine.size != 3) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for $command [name1] [name2] \n")
                return Command(Commands.LINK, splitLine[1], splitLine[2])
            }
            "unlink"->{
                if (splitLine.size != 2) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for $command [name] \n")
                return Command(Commands.UNLINK, splitLine[1])
            }
            "truncate"->{
                if (splitLine.size != 3) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for $command [name] [size] \n")
                return Command(Commands.TRUNC, splitLine[1], splitLine[2])
            }
            "mkdir"->{
                if (splitLine.size != 2) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for $command [name] \n")
                return Command(Commands.MKDIR, splitLine[1])
            }
            "rmdir"->{
                if (splitLine.size != 2) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for $command [name] \n")
                return Command(Commands.RMDIR, splitLine[1])
            }
            "cd"->{
                if (splitLine.size != 2) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for $command [path] \n")
                return Command(Commands.CD, splitLine[1])
            }
            "symlink"->{
                if (splitLine.size != 3) throw SyntaxError("Wrong format for command $command \n" +
                        "expected for $command [name] \n")
                return Command(Commands.SYMLINK, splitLine[1], splitLine[2])
            }
            "pwd"->{
                return Command(Commands.PWD)
            }

          else->throw SyntaxError("No such command found as $command")
        }
    }
}



