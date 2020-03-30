package com.kotlin.caight.websocket

enum class ResponseId(val id: Int) {
    UNKNOWN(-1),

    /*
     * EVALUATE_EMAIL(0)
     */
    UNKNOWN_EMAIL(0),
    REGISTERED_EMAIL(1),
    VERIFIED_EMAIL(2),

    /*
     * REGISTER_EMAIL(1)
     */
    REGISTER_OK(3),
    REGISTER_NO(4),

    /*
     * VERIFY_EMAIL_WEB_ONLY(2)
     */
    VERIFY_OK_WEB_ONLY(5),
    VERIFY_NO_WEB_ONLY(6),

    /*
     * SIGN_IN(3)
     */
    SIGN_IN_OK(7),
    SIGN_IN_WRONG_PW(8),
    SING_IN_ERROR(9),

    /*
     * NEW_GROUP(4)
     * NEW_CAT(5)
     */
    ADD_ENTITY_OK(10),
    ADD_ENTITY_NO(11),
    ADD_ENTITY_NOT_PW(12),
    ADD_ENTITY_ERROR(13),

    /*
     * DOWNLOAD_ENTITY(6)
     */
    ENTITY_GROUP(14),
    ENTITY_CAT(15),
    END_OF_ENTITY(16),
    DOWNLOAD_REJECTED(17),

    /*
     * CHANGE_NAME(7)
     */
    CHANGE_NAME_OK(18),
    CHANGE_NAME_NO(19),

    /*
     * LOGOUT(8)
     */
    LOGOUT_OK(20),
    LOGOUT_NO(21),

    /*
     * REQUEST_RESET_PASSWORD_URI(9)
     */
    RESET_PASSWORD_URI_CREATED(22),
    RESET_PASSWORD_URI_ERROR(23),

    /*
     * RESET_PASSWORD_WEB_ONLY(10)
     */
    RESET_PASSWORD_OK_WEB_ONLY(24),
    RESET_PASSWORD_EXPIRED_WEB_ONLY(25),
    RESET_PASSWORD_USED_WEB_ONLY(26),
    RESET_PASSWORD_NO_WEB_ONLY(27),

    /*
     * RESET_PASSWORD_CONFIRM_WEB_ONLY(11)
     */
    RESET_PASSWORD_CONFIRM_OK_WEB_ONLY(28),
    RESET_PASSWORD_CONFIRM_NO_WEB_ONLY(29),
    RESET_PASSWORD_CONFIRM_ERROR_WEB_ONLY(30),

    /*
     * DELETE_ACCOUNT(12)
     */
    DELETE_ACCOUNT_OK(31),
    DELETE_ACCOUNT_NO(32),

    /*
     * JOIN_GROUP(13)
     */
    JOIN_GROUP_OK(33),
    JOIN_GROUP_NOT_EXISTS(34),
    JOIN_GROUP_WRONG_PASSWORD(35),
    JOIN_GROUP_REJECTED(36),
    JOIN_GROUP_ERROR(37),

    /*
     * DOWNLOAD_MEMBER(14)
     */
    DOWNLOAD_MEMBER_ERROR(38),
    END_OF_MEMBER(39),

    /*
     * UPDATE_GROUP(15)
     */
    UPDATE_GROUP_OK(40),
    UPDATE_GROUP_ERROR(41),

    /*
     * DROP_GROUP(16)
     */
    DROP_GROUP_OK(42),
    DROP_GROUP_MEMBER_EXISTS(43),
    DROP_GROUP_ERROR(44),

    /*
     * OUT_GROUP(17)
     */
    WITHDRAW_GROUP_OK(45),
    WITHDRAW_GROUP_ERROR(46),

    /*
     * DROP_CAT(18)
     */
    DROP_CAT_OK(47),
    DROP_CAT_ERROR(48),

    /*
     * EDIT_CAT(19)
     */
    EDIT_CAT_OK(49),
    EDIT_CAT_ERROR(50),

    /*
     * UPLOAD_WEIGHT(20)
     */
    UPLOAD_WEIGHT_OK(51),
    UPLOAD_WEIGHT_ERROR(52);

    companion object {
        fun fromId(id: Int): ResponseId
        {
            for (req in values())
            {
                if (req.id == id) {
                    return req;
                }
            }

            return UNKNOWN
        }
    }
}