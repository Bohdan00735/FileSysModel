package errors

abstract class MyError(override val message: String): Error() {
}