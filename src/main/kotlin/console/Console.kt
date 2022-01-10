package console

import errors.MyError
import MySystem
import command.CommandsParser

class Console {
    private val system = MySystem()
    private val commandsParser = CommandsParser()
    fun start(){
        println("Hello in Console")
        println("\$")

        while(true){
            val line = readLine()
            if (readLine() != null){
                try {
                    system.doCommand(commandsParser.parseLine(line!!))

                }catch (error: MyError){
                    println(error.message)
                }
            }
            println("\$")
        }
    }
}