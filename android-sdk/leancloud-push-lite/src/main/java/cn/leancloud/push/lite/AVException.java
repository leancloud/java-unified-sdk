package cn.leancloud.push.lite;

public class AVException extends Exception {
  private static final long serialVersionUID = 1L;
  protected int code;
  public static final int OTHER_CAUSE = -1;
  /**
   * Error code indicating that something has gone wrong with the server. If you get this error
   * code, it is AVOSCloud's fault.
   */
  public static final int INTERNAL_SERVER_ERROR = 1;
  /**
   * Error code indicating the connection to the AVOSCloud servers failed.
   */
  public static final int CONNECTION_FAILED = 100;
  /**
   * Error code indicating the specified object doesn't exist.
   */
  public static final int OBJECT_NOT_FOUND = 101;
  /**
   * Error code indicating you tried to query with a datatype that doesn't support it, like exact
   * matching an array or object.
   */
  public static final int INVALID_QUERY = 102;
  /**
   * Error code indicating a missing or invalid classname. Classnames are case-sensitive. They must
   * start with a letter, and a-zA-Z0-9_ are the only valid characters.
   */
  public static final int INVALID_CLASS_NAME = 103;
  /**
   * Error code indicating an unspecified object id.
   */
  public static final int MISSING_OBJECT_ID = 104;
  /**
   * Error code indicating an invalid key name. Keys are case-sensitive. They must start with a
   * letter, and a-zA-Z0-9_ are the only valid characters.
   */
  public static final int INVALID_KEY_NAME = 105;
  /**
   * Error code indicating a malformed pointer. You should not see this unless you have been mucking
   * about changing internal AVOSCloud code.
   */
  public static final int INVALID_POINTER = 106;
  /**
   * Error code indicating that badly formed JSON was received upstream. This either indicates you
   * have done something unusual with modifying how things encode to JSON, or the network is failing
   * badly.
   */
  public static final int INVALID_JSON = 107;
  /**
   * Error code indicating that the feature you tried to access is only available internally for
   * testing purposes.
   */
  public static final int COMMAND_UNAVAILABLE = 108;
  /**
   * You must call AVOSCloud.initialize before using the AVOSCloud library.
   */
  public static final int NOT_INITIALIZED = 109;
  /**
   * Error code indicating that a field was set to an inconsistent type.
   */
  public static final int INCORRECT_TYPE = 111;
  /**
   * Error code indicating an invalid channel name. A channel name is either an empty string (the
   * broadcast channel) or contains only a-zA-Z0-9_ characters and starts with a letter.
   */
  public static final int INVALID_CHANNEL_NAME = 112;
  /**
   * Error code indicating that push is misconfigured.
   */
  public static final int PUSH_MISCONFIGURED = 115;
  /**
   * Error code indicating that the object is too large.
   */
  public static final int OBJECT_TOO_LARGE = 116;
  /**
   * Error code indicating that the operation isn't allowed for clients.
   */
  public static final int OPERATION_FORBIDDEN = 119;
  /**
   * Error code indicating the result was not found in the cache.
   */
  public static final int CACHE_MISS = 120;
  /**
   * Error code indicating that an invalid key was used in a nested JSONObject.
   */
  public static final int INVALID_NESTED_KEY = 121;
  /**
   * Error code indicating that an invalid filename was used for AVFile. A valid file name contains
   * only a-zA-Z0-9_. characters and is between 1 and 128 characters.
   */
  public static final int INVALID_FILE_NAME = 122;
  /**
   * Error code indicating an invalid ACL was provided.
   */
  public static final int INVALID_ACL = 123;
  /**
   * Error code indicating that the request timed out on the server. Typically this indicates that
   * the request is too expensive to run.
   */
  public static final int TIMEOUT = 124;
  /**
   * Error code indicating that the email address was invalid.
   */
  public static final int INVALID_EMAIL_ADDRESS = 125;
  /**
   * Error code indicating that the file address was invalid.
   */
  public static final int INVALID_FILE_URL = 126;

  /**
   * 用来标识手机号码格式错误的错误代码
   */
  public static final int INVALID_PHONE_NUMBER = 127;
  /**
   * Error code indicating that a unique field was given a value that is already taken.
   */
  public static final int DUPLICATE_VALUE = 137;
  /**
   * Error code indicating that a role's name is invalid.
   */
  public static final int INVALID_ROLE_NAME = 139;
  /**
   * Error code indicating that an application quota was exceeded. Upgrade to resolve.
   */
  public static final int EXCEEDED_QUOTA = 140;
  /**
   * Error code indicating that a Cloud Code script failed.
   */
  public static final int SCRIPT_ERROR = 141;
  /**
   * Error code indicating that cloud code validation failed.
   */
  public static final int VALIDATION_ERROR = 142;
  /**
   * Error code indicating that deleting a file failed.
   */
  public static final int FILE_DELETE_ERROR = 153;
  /**
   * Error code indicating that the username is missing or empty.
   */
  public static final int USERNAME_MISSING = 200;
  /**
   * Error code indicating that the password is missing or empty.
   */
  public static final int PASSWORD_MISSING = 201;
  /**
   * Error code indicating that the username has already been taken.
   */
  public static final int USERNAME_TAKEN = 202;
  /**
   * Error code indicating that the email has already been taken.
   */
  public static final int EMAIL_TAKEN = 203;
  /**
   * Error code indicating that the email is missing, but must be specified.
   */
  public static final int EMAIL_MISSING = 204;
  /**
   * Error code indicating that an user with the specified email was not found.
   */
  public static final int EMAIL_NOT_FOUND = 205;
  /**
   * Error code indicating that an user object without a valid session could not be altered.
   */
  public static final int SESSION_MISSING = 206;
  /**
   * Error code indicating that an user can only be created through signup.
   */
  public static final int MUST_CREATE_USER_THROUGH_SIGNUP = 207;
  /**
   * Error code indicating that an an account being linked is already linked to another user.
   */
  public static final int ACCOUNT_ALREADY_LINKED = 208;
  /**
   * Error code indicating that User ID mismatch.
   */
  public static final int USER_ID_MISMATCH = 209;
  /**
   * Error code indicating that username and password mismatched.
   */
  public static final int USERNAME_PASSWORD_MISMATCH = 210;

  /**
   * Error code indicating that user doesn't exist
   */
  public static final int USER_DOESNOT_EXIST = 211;

  /**
   * 　用户并没有绑定手机号码
   */
  public static final int USER_MOBILEPHONE_MISSING = 212;

  /**
   * 　没有找到绑定了该手机号的用户
   */
  public static final int USER_WITH_MOBILEPHONE_NOT_FOUND = 213;

  /**
   * 这个号码已经绑定过别的账号了
   */
  public static final int USER_MOBILE_PHONENUMBER_TAKEN = 214;

  /**
   * 这个手机号码尚未被验证过
   */
  public static final int USER_MOBILEPHONE_NOT_VERIFIED = 215;
  /**
   * Error code indicating that an user cannot be linked to an account because that account's id
   * could not be found.
   */
  public static final int LINKED_ID_MISSING = 250;
  /**
   * Error code indicating that an user with a linked (e.g. Facebook) account has an invalid
   * session.
   */
  public static final int INVALID_LINKED_SESSION = 251;
  /**
   * Error code indicating that a service being linked (e.g. Facebook or Twitter) is unsupported.
   */
  public static final int UNSUPPORTED_SERVICE = 252;

  /**
   * Error code indicating client is rate limited by avoscloud server.
   */
  public static final int RATE_LIMITED = 503;

  /**
   * Error code indicating unknown reason.
   */
  public static final int UNKNOWN = 999;

  public static final String cacheMissingErrorString = "Cache Missing";

  /**
   * Error code indicating the file checkSum value is not equals to original file
   */
  public static final int FILE_DOWNLOAD_INCONSISTENT_FAILURE = 253;

  /**
   * Construct a new AVException with a particular error code.
   *
   * @param theCode The error code to identify the type of exception.
   * @param theMessage A message describing the error in more detail.
   */
  public AVException(int theCode, String theMessage) {
    super(theMessage);
    this.code = theCode;
  }

  /**
   * Construct a new AVException with an external cause.
   *
   * @param message A message describing the error in more detail.
   * @param cause The cause of the error.
   */
  public AVException(String message, Throwable cause) {
    super(message, cause);
    if (cause instanceof AVException) {
      this.code = ((AVException) cause).getCode();
    }
  }


  /**
   * Construct a new AVException with an external cause.
   *
   * @param cause The cause of the error.
   */
  public AVException(Throwable cause) {
    super(cause);
    if (cause instanceof AVException) {
      this.code = ((AVException) cause).getCode();
    }
  }

  /**
   * Access the code for this error.
   *
   * @return The numerical code for this error.
   */
  public int getCode() {
    return code;
  }
}