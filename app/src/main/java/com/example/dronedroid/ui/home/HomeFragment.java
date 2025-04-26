package com.example.dronedroid.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.lifecycle.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dronedroid.R;
import com.example.dronedroid.databinding.FragmentHomeBinding;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private TextView textView, imuTextView;
    private Button thrtleMinusBtn, thrtlePlusBtn;
    private SeekBar throttleS, SGS, FSS;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //ViewModelの取得
        HomeViewModel homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        //fragmentの取得
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //Bluetoothの接続状態の監視とコールバックの紐づけ
        final Observer<Boolean> btConnectObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if (isConnected){
                    Log.i("HomeFragment", "Connection State True");
                    setConnState();
                }else {
                    Log.i("HomeFragment", "Connection State false");
                    setDisconState();
                }
            }
        };
        homeViewModel.getIsConnected().observe(getViewLifecycleOwner(), btConnectObserver);

        //送受信メッセージの監視とTextViewの紐づけ
        textView = binding.msgWindow;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //スロットル指定用のシークバー　初期値は操作不可
        throttleS = binding.throttleSeekBar;
        throttleS.setEnabled(false);
        final SeekBar.OnSeekBarChangeListener onSeekChange = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ArrayList<Float> u = new ArrayList<>(5);
                if (seekBar.getId() == R.id.throttleSeekBar){
                    Log.i("HomeFragment", "onValueChang");
                    //ViewModelの値を更新
                    u.set(0, (float)progress);
                }
                else if (seekBar.getId() == R.id.SGSeekBar){
                    Log.i("HomeFragment", "onValueChang");
                    //ViewModelの値を更新
                    u.set(1, (float)progress);
                }
                else if (seekBar.getId() == R.id.FSSeekBar){
                    Log.i("HomeFragment", "onValueChang");
                    u.set(2, (float)progress);
                }

                homeViewModel.getControl().postValue(u);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        throttleS.setOnSeekBarChangeListener(onSeekChange);

        //スロットル微調整用のボタン　減らし用　初期状態で操作不可
        thrtleMinusBtn = binding.throttleMinusBtn;
        thrtleMinusBtn.setEnabled(false);
        thrtleMinusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(throttleS.getProgress() > 0) throttleS.setProgress(throttleS.getProgress() - 1);
            }
        });
        //スロットル微調整用のボタン　増やし用　初期状態で操作不可
        thrtlePlusBtn = binding.throttlePlusBtn;
        thrtlePlusBtn.setEnabled(false);
        thrtlePlusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(throttleS.getProgress() < 100)throttleS.setProgress(throttleS.getProgress() + 1);
            }
        });

        //Servo SG用のシークバー　初期値は操作不可
        SGS = binding.SGSeekBar;
        SGS.setEnabled(false);
        SGS.setOnSeekBarChangeListener(onSeekChange);

        //Servo FS用のシークバー　初期値は操作不可
        FSS = binding.FSSeekBar;
        FSS.setEnabled(false);
        FSS.setOnSeekBarChangeListener(onSeekChange);

        return root;
    }

    @Override
    public void onDestroyView() {
        setDisconState();
        super.onDestroyView();
        binding = null;
    }

    private void setDisconState(){
        throttleS.setEnabled(false);
        //throttleS.setProgress(0);
        thrtleMinusBtn.setEnabled(false);
        thrtlePlusBtn.setEnabled(false);
        SGS.setEnabled(false);
        FSS.setEnabled(false);
    }
    private void setConnState(){
        throttleS.setEnabled(true);

        thrtleMinusBtn.setEnabled(true);
        thrtlePlusBtn.setEnabled(true);
        SGS.setEnabled(true);
        FSS.setEnabled(true);
    }

}