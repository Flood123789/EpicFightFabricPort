package yesman.epicfight.forgecompat.fml;

public class ModLoadingException extends RuntimeException {
	public ModLoadingException(String message) {
		super(message);
	}

	public ModLoadingException(String message, Throwable cause) {
		super(message, cause);
	}
}
