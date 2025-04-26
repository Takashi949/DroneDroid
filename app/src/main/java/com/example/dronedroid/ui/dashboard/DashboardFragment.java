package com.example.dronedroid.ui.dashboard;

import android.opengl.GLU;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.dronedroid.databinding.FragmentDashboardBinding;
import com.example.dronedroid.ui.home.HomeViewModel;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.io.IOException;
import java.nio.*;
import java.util.ArrayList;

import android.util.DisplayMetrics;

public class DashboardFragment extends Fragment {
    private GLSurfaceView gLView;
    private GLRenderer mRenderer;
    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;

        gLView = new GLSurfaceView(getContext());
        gLView.setEGLContextClientVersion(1);

        // GLSurfaceViewのインスタンスを作成
        gLView = new GLSurfaceView(getContext());

        // DisplayMetricsを取得
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // 画面の幅と高さを取得
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        // レイアウトパラメータを作成し、幅と高さを指定
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width , height -280);

        // GLSurfaceViewにレイアウトパラメータを適用
        gLView.setLayoutParams(params);

        mRenderer = new GLRenderer();
        try {
            mRenderer.loadSTL(getResources().getAssets().open("droneAssyv15.stl"));
        }catch (IOException e){
            Log.e("Dashboard", e.getMessage());
        }

        gLView.setRenderer(mRenderer);

        gLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // Add the GLSurfaceView to the layout
        ((ViewGroup) root).addView(gLView);

        //Bluetoothの接続状態の監視とコールバックの紐づけ
        final Observer<Boolean> btConnectObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if (isConnected){
                    Log.i("HomeFragment", "Connection State True");
                }else {
                    Log.i("HomeFragment", "Connection State false");
                }
            }
        };
        //IMUのメッセージをパース
        homeViewModel.getIsConnected().observe(getViewLifecycleOwner(), btConnectObserver);
        homeViewModel.getPitchRollYaw().observe(getViewLifecycleOwner(), new Observer<ArrayList<Float>>() {
            @Override
            public void onChanged(ArrayList<Float> rpy) {
                mRenderer.setAngle(rpy.get(0), rpy.get(1), rpy.get(2));
            }
        });
        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
        gLView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        gLView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}