package com.example.dronedroid.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<String> msgText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Float>> PitchRollYaw = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Float>> position = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Float>> velocity = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Float>> accel = new MutableLiveData<>();

    public MutableLiveData<ArrayList<Float>> getControl() {
        return control;
    }

    private final MutableLiveData<ArrayList<Float>> control = new MutableLiveData<>();

    public MutableLiveData<String> throttleText = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isContrlEnable = new MutableLiveData<>();

    public MutableLiveData<ArrayList<Float>> getPosition() {
        return position;
    }

    public MutableLiveData<ArrayList<Float>> getVelocity() {
        return velocity;
    }

    public MutableLiveData<ArrayList<Float>> getAccel() {
        return accel;
    }

    public HomeViewModel(){
        msgText.setValue("Not Connected");
        isConnected.setValue(false);
        control.setValue(new ArrayList<>(Arrays.asList(0f, 50f, 50f, 50f, 50f)));
        PitchRollYaw.setValue(new ArrayList<>(Arrays.asList(0f, 0f, 0f)));
        isContrlEnable.setValue(false);
    }
    public MutableLiveData<String>  getText() {
        return msgText;
    }

    public MutableLiveData<Boolean> getIsConnected() {
        return isConnected;
    }
    public MutableLiveData<ArrayList<Float>> getPitchRollYaw() {
        return PitchRollYaw;
    }

    public MutableLiveData<Boolean> getIsContrlEnable() {
        return isContrlEnable;
    }
    public MutableLiveData<String> getThrottleText() {
        return throttleText;
    }

}