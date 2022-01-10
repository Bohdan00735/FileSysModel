class Directory {
    private val fileLinks = ArrayList<FileLink>()

    override fun toString(): String {
        val result = StringBuilder()
        for (link in fileLinks){
            result.append(fileLinks.toString() + "\n")
        }
        return result.toString()
    }
}