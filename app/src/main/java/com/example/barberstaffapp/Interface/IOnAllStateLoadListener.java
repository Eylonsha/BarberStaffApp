package com.example.barberstaffapp.Interface;

import com.example.barberstaffapp.Model.City;

import java.util.List;

public interface IOnAllStateLoadListener {
    void onAllStateLoadListenerSuccess(List<City> cityList);
    void onAllStateLoadListenerFailure(String Message);
}
