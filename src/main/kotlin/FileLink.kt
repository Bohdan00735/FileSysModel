class FileLink(var fileName: String, val descriptorLink: Int) {
    private var isLinkValid = true

    override fun toString(): String {
        return "$fileName:$descriptorLink:$isLinkValid"
    }
}