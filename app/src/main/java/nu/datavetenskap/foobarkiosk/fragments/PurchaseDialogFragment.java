package nu.datavetenskap.foobarkiosk.fragments;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Response;
import com.google.gson.Gson;
import com.sumup.merchant.api.SumUpAPI;
import com.sumup.merchant.api.SumUpLogin;
import com.sumup.merchant.api.SumUpPayment;

import java.math.BigDecimal;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import nu.datavetenskap.foobarkiosk.Elements.PurchaseButton;
import nu.datavetenskap.foobarkiosk.FoobarAPI;
import nu.datavetenskap.foobarkiosk.R;
import nu.datavetenskap.foobarkiosk.models.IAccount;
import nu.datavetenskap.foobarkiosk.models.statemodels.IState;


public class PurchaseDialogFragment extends DialogFragment implements
        View.OnClickListener {
    private FragmentActivity activity;
    private static final String TAG = "PurchaseDialogFragment";
    private OnPurchaseDialogInteractionListener mListener;
    private static IState activeState;
    private static Gson gson;
    private ResponseDialog responseDialog;

    @BindView(R.id.purchase_cash_btn) PurchaseButton _cashBtn;
    @BindView(R.id.purchase_foocash_btn) PurchaseButton _fooCBtn;
    @BindView(R.id.purchase_credit_btn) PurchaseButton _creditBtn;
    @BindView(R.id.purchase_cancel_btn) Button _cancelBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_purchase, container, false);

        ButterKnife.bind(this, view);

        determineFooCashAbility();

        _cashBtn.setOnClickListener(this);
        _fooCBtn.setOnClickListener(this);
        _creditBtn.setOnClickListener(this);
        _cancelBtn.setOnClickListener(this);

        // Disabled until further development and testing
        _creditBtn.setEnabled(false);
        _creditBtn.setText("To be announced later");

        return view;
    }

    private void determineFooCashAbility() {
        if (!getArguments().getBoolean("loggedIn")){
            _fooCBtn.setEnabled(false);
            _fooCBtn.setText("Not logged in with foo card");
            return;
        }
        if (getArguments().getFloat("balance") < getArguments().getFloat("costTotal")) {
            _fooCBtn.setEnabled(false);
            _fooCBtn.setText("Insufficient funds");
            return;
        }
        _fooCBtn.setEnabled(true);
        _fooCBtn.setText("FooCash\n" + getArguments().getString("name"));

    }

    public static PurchaseDialogFragment newInstance(IState state, ResponseDialog _responseDialog) {
        PurchaseDialogFragment f = new PurchaseDialogFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        Log.d(TAG, "New instance");
        IAccount account = state.getAccount();
        if (account != null) {
            args.putBoolean("loggedIn", true);
            args.putString("name", state.getAccount().getName());
            args.putFloat("balance", state.getAccount().getBalance());
        }
        else {
            args.putBoolean("loggedIn", false);
        }
        gson = new Gson();
        activeState = state;
        f.responseDialog = _responseDialog;
        args.putString("purchaseState", gson.toJson(state));
        args.putFloat("costTotal", state.getPurchaseCost());
        f.setArguments(args);

        return f;

    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getFragmentManager();

        switch (v.getId()) {
            case R.id.purchase_cash_btn:
                cashPurchase();
                // DialogFragment.show() will take care of adding the fragment
                // in a transaction.
                dismiss();
                // Create and show the dialog.
                responseDialog.show(fm, "dialog");
                break;
            case R.id.purchase_foocash_btn:
                fooCashPurchase();
                dismiss();
                responseDialog.show(fm, "dialog");
                break;
            case R.id.purchase_credit_btn:
                // Disabled until further development and testing
                //creditCardPurchase();
                break;
            case R.id.purchase_cancel_btn:
                dismiss();
            default:
                break;
        }
    }

    private void cashPurchase() {
        //final String purchaseState = getArguments().getString("purchaseState");
        //FoobarAPI.sendCashPurchaseRequest(gson.fromJson(purchaseState, IState.class));
        FoobarAPI.sendCashPurchaseRequest(activeState,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseDialog.onSuccess(activeState.getPurchaseCost());
                        mListener.onPurchaseSuccess();
                    }
                });
        dismiss();
    }

    private void fooCashPurchase() {
//        final String purchaseState = getArguments().getString("purchaseState");
//        FoobarAPI.sendCashPurchaseRequest(gson.fromJson(purchaseState, IState.class));
        FoobarAPI.sendFooCashPurchaseRequest(activeState,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseDialog.onSuccess(activeState.getPurchaseCost(), activeState.getAccount());
                        mListener.onPurchaseSuccess();
                    }
                });
    }

    // Disabled until further development and testing
    private void creditCardPurchase() {
        testPurchase();
    }

    private void testLogin() {
        SumUpLogin sumUplogin = SumUpLogin.builder("ThisIsDummyText").build();
        SumUpAPI.openLoginActivity(activity, sumUplogin, REQUEST_CODE_LOGIN);
    }

    private void testPurchase() {
        SumUpPayment payment = SumUpPayment.builder()

                // mandatory parameters
                .total(new BigDecimal(6)) // minimum 1.00
                .currency(SumUpPayment.Currency.EUR)
                // optional: include a tip amount in addition to the total
                // .tip(new BigDecimal("0.10"))

                // optional: add details
                .title("Foobar Kiosk")
                // .receiptEmail("customer@mail.com")
                // .receiptSMS("+3531234567890")

                // optional: Add metadata
                //.addAdditionalInfo("Delicatoboll", "6 kr")
                // optional: foreign transaction ID, must be unique!
                .foreignTransactionId(UUID.randomUUID().toString())  // can not exceed 128 chars

                // optional: skip the success screen
                //.skipSuccessScreen()
                .build();

        SumUpAPI.checkout(activity, payment, REQUEST_CODE_PAYMENT);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
    }

    public void updateAccessRights(final IAccount account) {
        if (activeState.getAccount() == null) {
            activeState.setAccount(account);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (account == null){
                        _fooCBtn.setEnabled(false);
                        _fooCBtn.setText("Not logged in with foo card");
                        return;
                    }
                    if (account.getBalance() < getArguments().getFloat("costTotal")) {
                        _fooCBtn.setEnabled(false);
                        _fooCBtn.setText("Insufficient funds");
                        return;
                    }
                    Log.d(TAG, "Balance: " + account.getBalance());
                    Log.d(TAG, "Cost: " + getArguments().getFloat("costTotal"));
                    _fooCBtn.setEnabled(true);
                    _fooCBtn.setText("FooCash\n" + account.getName());
                }
            });
        }


    }

    private static final int REQUEST_CODE_LOGIN = 1;
    private static final int REQUEST_CODE_PAYMENT = 2;
    private static final int REQUEST_CODE_PAYMENT_SETTINGS = 3;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode) {
            case REQUEST_CODE_LOGIN:
                if (data != null) {
                    Bundle extra = data.getExtras();
                    Log.e("SumUp Payment", "Result code: " + extra.getInt(SumUpAPI.Response.RESULT_CODE));
                    Log.e("SumUp Payment", "Message: " + extra.getString(SumUpAPI.Response.MESSAGE));
                }
                break;

            case REQUEST_CODE_PAYMENT:
                if (data != null) {
                    Bundle extra = data.getExtras();

                    Log.e("SumUp Payment", "Result code: " + extra.getInt(SumUpAPI.Response.RESULT_CODE));
                    Log.e("SumUp Payment", "Message: " + extra.getString(SumUpAPI.Response.MESSAGE));

//                    String txCode = extra.getString(SumUpAPI.Response.TX_CODE);
//                    mTxCode.setText(txCode == null ? "" : "Transaction Code: " + txCode);
//
//                    boolean receiptSent = extra.getBoolean(SumUpAPI.Response.RECEIPT_SENT);
//                    mReceiptSent.setText("Receipt sent: " + receiptSent);
//
//                    TransactionInfo transactionInfo = extra.getParcelable(SumUpAPI.Response.TX_INFO);
//                    mTxInfo.setText(transactionInfo == null ? "" : "Transaction Info : " + transactionInfo);
                }
                break;

            case REQUEST_CODE_PAYMENT_SETTINGS:
                if (data != null) {
                    Bundle extra = data.getExtras();
                    Log.e("SumUp Payment", "Result code: " + extra.getInt(SumUpAPI.Response.RESULT_CODE));
                    Log.e("SumUp Payment", "Message: " + extra.getString(SumUpAPI.Response.MESSAGE));
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPurchaseDialogInteractionListener) {
            mListener = (OnPurchaseDialogInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPurchaseDialogInteractionListener");
        }
    }


    public interface OnPurchaseDialogInteractionListener {
        void onPurchaseSuccess();
        void onPurchaseDialogDismiss();
        void onPurchaseDialogPurchaseSuccess();
    }
}
