package com.example.splashscreenfinal;

import android.content.Context;
import android.content.SharedPreferences;

class PrefManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;
    private String EMAIL_ID = "EmailID";
    private String PREF_NAME = "com.example.splashscreenfinal";
    private String IS_PERMISSION_SET = "IsPermissionSet";
    private String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private String IS_FORM_FILLED = "IsFormFilled";
    private String IS_GESTURE_SET = "IsGestureSet";
    private String SINGLE_TAP = "SingleTap";
    private String DOUBLE_TAP = "DoubleTap";
    private String LONG_PRESS = "LongPress";
    private String SWIPE_UP = "SwipeUp";
    private String SWIPE_DOWN = "SwipeDown";
    private String SWIPE_LEFT = "SwipeLeft";
    private String SWIPE_RIGHT = "SwipeRight";
    private String BLIND_CONTACT = "BlindContact";
    private String BLIND_NAME = "BlindName";
    private String GUARDIAN_CONTACT = "GuardianContact";
    private String SPEECH_RATE = "SpeechRate";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public String getSPEECH_RATE() {
        return pref.getString(SPEECH_RATE,"1f");
    }

    public void setSPEECH_RATE(String rate) {
        editor.putString(SPEECH_RATE, rate);
        editor.commit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, false);
    }

    public boolean isFormFilled() {
        return pref.getBoolean(IS_FORM_FILLED, false);
    }

    public boolean getIsGestureSet() {
        return pref.getBoolean(IS_GESTURE_SET, false);
    }

    public void setIsGestureSet(boolean isGestureSet) {
        editor.putBoolean(IS_GESTURE_SET, isGestureSet);
        editor.commit();
    }

    public void setFormFilled(boolean isFilled){
        editor.putBoolean(IS_FORM_FILLED, isFilled);
        editor.commit();
    }

    public boolean getIS_PERMISSION_SET() {
        return pref.getBoolean(IS_PERMISSION_SET, false);
    }

    public void setIS_PERMISSION_SET(boolean yesNo) {
        editor.putBoolean(IS_PERMISSION_SET, yesNo);
        editor.commit();
    }

    public String getSingleTap() {
        return pref.getString(SINGLE_TAP, "");
    }

    public String getDoubleTap() {
        return pref.getString(DOUBLE_TAP, "");
    }

    public String getLongPress() {
        return pref.getString(LONG_PRESS, "");
    }

    public String getSwipeUp() {
        return pref.getString(SWIPE_UP, "");
    }

    public String getSwipeDown() {
        return pref.getString(SWIPE_DOWN, "");
    }

    public String getSwipeLeft() {
        return pref.getString(SWIPE_LEFT, "");
    }

    public String getSwipeRight() {
        return pref.getString(SWIPE_RIGHT, "");
    }

    public void setSingleTap(String singleTap) {
        editor.putString(SINGLE_TAP, singleTap);
        editor.commit();
    }

    public void setDoubleTap(String doubleTap) {
        editor.putString(DOUBLE_TAP, doubleTap);
        editor.commit();    }

    public void setLongPress(String longPress) {
        editor.putString(LONG_PRESS, longPress);
        editor.commit();    }

    public void setSwipeUp(String swipeUp) {
        editor.putString(SWIPE_UP, swipeUp);
        editor.commit();    }

    public void setSwipeDown(String swipeDown) {
        editor.putString(SWIPE_DOWN, swipeDown);
        editor.commit();    }

    public void setSwipeLeft(String swipeLeft) {
        editor.putString(SWIPE_LEFT, swipeLeft);
        editor.commit();
    }

    public void setSwipeRight(String swipeRight) {
        editor.putString(SWIPE_RIGHT, swipeRight);
        editor.commit();
    }

    public void setBlindContact(String blindContact) {
        editor.putString(BLIND_CONTACT, blindContact);
        editor.commit();    }

    public void setBlindName(String blindName) {
        editor.putString(BLIND_NAME, blindName);
        editor.commit();
    }

    public void setGuardianContact(String guardianContact) {
        editor.putString(GUARDIAN_CONTACT, guardianContact);
        editor.commit();
    }

    public String getBlindContact() {
        return pref.getString(BLIND_CONTACT,null);
    }

    public String getBlindName() {
        return pref.getString(BLIND_NAME,null);
    }

    public String getGuardianContact() {
        return pref.getString(GUARDIAN_CONTACT,null);
    }

    public String getEMAIL_ID() {return pref.getString(EMAIL_ID, "");}

    public void setEMAIL_ID(String email) {
        editor.putString(EMAIL_ID, email);
        editor.commit();
    }


}
