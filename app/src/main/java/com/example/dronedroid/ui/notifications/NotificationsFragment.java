package com.example.dronedroid.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.dronedroid.databinding.FragmentNotificationsBinding;
import com.example.dronedroid.ui.home.HomeViewModel;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private TextView postionTextView, velTextView, acceTextView, ctrltextView;
    Button BCButton, ECButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //制御開始ボタン
        BCButton = binding.beginControl;
        BCButton.setEnabled(false);
        BCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.getIsContrlEnable().setValue(true);
            }
        });

        //制御終了ボタン
        ECButton = binding.endControl;
        ECButton.setEnabled(false);
        ECButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeViewModel.getIsContrlEnable().setValue(false);
            }
        });

        //Bluetoothの接続状態の監視とコールバックの紐づけ
        final Observer<Boolean> btConnectObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if (isConnected){
                    Log.i("NotifFragment", "Connection State True");
                    setConnState();
                }else {
                    Log.i("NotifFragment", "Connection State false");
                    setDisconState();
                }
            }
        };
        homeViewModel.getIsConnected().observe(getViewLifecycleOwner(), btConnectObserver);

        //位置のメッセージを表示
        postionTextView = binding.positionTextView;
        homeViewModel.getPosition().observe(getViewLifecycleOwner(), new Observer<ArrayList<Float>>() {
            @Override
            public void onChanged(ArrayList<Float> pos) {
                postionTextView.setText("x:"
                        + String.valueOf(pos.get(0)) + ","
                        + String.valueOf(pos.get(1)) + ","
                        + String.valueOf(pos.get(2)));
            }
        });

        //速度のメッセージを表示
        velTextView = binding.velocityTextView;
        homeViewModel.getVelocity().observe(getViewLifecycleOwner(), new Observer<ArrayList<Float>>() {
            @Override
            public void onChanged(ArrayList<Float> vel) {
                velTextView.setText("v:"
                        + String.valueOf(vel.get(0)) + ","
                        + String.valueOf(vel.get(1)) + ","
                        + String.valueOf(vel.get(2)));
            }
        });

        //加速度のメッセージを表示
        acceTextView = binding.accelTextView;
        homeViewModel.getAccel().observe(getViewLifecycleOwner(), new Observer<ArrayList<Float>>() {
            @Override
            public void onChanged(ArrayList<Float> acc) {
                acceTextView.setText("a:"
                        + String.valueOf(acc.get(0)) + ","
                        + String.valueOf(acc.get(1)) + ","
                        + String.valueOf(acc.get(2)));
            }
        });

        //制御量の表示
        ctrltextView = binding.ctrltextView;
        homeViewModel.getControl().observe(getViewLifecycleOwner(), new Observer<ArrayList<Float>>() {
            @Override
            public void onChanged(ArrayList<Float> u) {
                ctrltextView.setText("u:"
                        + String.valueOf(u.get(0) + ","
                        + String.valueOf(u.get(1) + ",")
                        + String.valueOf(u.get(2) + ",")));
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void setDisconState(){
        BCButton.setEnabled(false);
        ECButton.setEnabled(false);
    }
    private void setConnState(){
        BCButton.setEnabled(true);
        ECButton.setEnabled(true);
    }
}