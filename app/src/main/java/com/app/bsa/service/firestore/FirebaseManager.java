package com.app.bsa.service.firestore;

public class FirebaseManager {

    private static FirebaseManager fm;
    private FirebaseManager(){

    }

    public static FirebaseManager  getInstance(){
        if(fm==null) {
            synchronized (fm) {
                fm = new FirebaseManager();
            }
        }
        return fm;
    }
}
