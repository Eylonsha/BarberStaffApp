package com.example.barberstaffapp.Interface;

import android.content.DialogInterface;

public interface IDialogClockListener {
    void onClickPositiveButton(DialogInterface dialogInterface,String userName,String password);
    void onClickNegativeButton(DialogInterface dialogInterface);
}
