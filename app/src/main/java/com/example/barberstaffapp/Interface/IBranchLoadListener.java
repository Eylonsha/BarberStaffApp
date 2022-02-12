package com.example.barberstaffapp.Interface;

import com.example.barberstaffapp.Model.Salon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> areaNameList);
    void onBranchLoadFaild(String message);
}
