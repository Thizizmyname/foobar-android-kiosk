package nu.datavetenskap.foobarkiosk.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Response;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import nu.datavetenskap.foobarkiosk.FoobarAPI;
import nu.datavetenskap.foobarkiosk.R;
import nu.datavetenskap.foobarkiosk.adapters.StoreAdapter;
import nu.datavetenskap.foobarkiosk.models.Category;
import nu.datavetenskap.foobarkiosk.models.IAccount;
import nu.datavetenskap.foobarkiosk.models.Product;
import nu.datavetenskap.foobarkiosk.models.StoreEntity;

/**
 * A placeholder fragment containing a simple view.
 */
public class StoreFragment extends Fragment {
    private static final int PRODUCT_COLUMNS = 4;

    CartFragment cartFragment;

    ArrayList<StoreEntity> storeProductList;
    ArrayList<StoreEntity> storeCategoriesList;
    ArrayList<StoreEntity> storeEntities;
    StoreAdapter storeEntityAdapter;
    GridLayoutManager mgridLayoutManager;
    boolean onCategoryView;

    @Bind(R.id.btn_get_products) Button _btnProducts;
    @Bind(R.id.btn_get_categories) Button _btnState;
    @Bind(R.id.btn_get_card) Button _btncard;
    @Bind(R.id.grid_view) RecyclerView _grid;
    @Bind(R.id.btn_get_random) Button _btnRand;

    public StoreFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store, container, false);

        ButterKnife.bind(this, view);
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());


        storeProductList = new ArrayList<>();
        storeCategoriesList = new ArrayList<>();
        storeEntities = new ArrayList<>();
        onCategoryView = true;


        mgridLayoutManager = new GridLayoutManager(getActivity(), PRODUCT_COLUMNS);
        _grid.setLayoutManager(mgridLayoutManager);
        //storeAdapter = new StoreAdapter(getContext(), storeProductList,
        storeEntityAdapter = new StoreAdapter(getContext(), storeEntities,
                pref.getString(getString(R.string.pref_key_fooapi_host), ""));
        StoreAdapter.setOnItemClickListener(new StoreAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                initiateEntityAction(position);
            }
        });
        _grid.setAdapter(storeEntityAdapter);

        cartFragment = (CartFragment) getChildFragmentManager().findFragmentById(R.id.store_sidebar);


        _btnProducts.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getProductCategories();
                getProductList();
                //retrieveProductFromBarcode("7310500088853");
                //retrieveAccountFromCard("1337");

            }
        });

        _btnState.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //getProductCategories();
                //updateStateWithVolley();

            }
        });
        _btncard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cartFragment.retrieveAccountFromCard("1337");
            }
        });

        _btnRand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] text = {"7340083438684", "7611612221351", "7310500088853", "1234567890"};
                Random r = new Random();
                int i = r.nextInt(text.length);

                cartFragment.retrieveProductFromBarcode(text[i]);
            }
        });

        return view;
    }

    private void initiateEntityAction(int position) {
        StoreEntity entity = storeEntities.get(position);

        if (entity instanceof Category) {
            String categoryId = entity.getId();
            storeEntities.clear();
            onCategoryView = false;
            for (StoreEntity e : storeProductList) {
                Product p = (Product) e;
                if (p.getCategory() != null && p.isActive() && p.getCategory().equals(categoryId)) {
                    storeEntities.add(e);
                }
            }
            storeEntities.add(0, new StoreEntity.BackButtonEntity());
            storeEntityAdapter.notifyDataSetChanged();
        }
        else if(entity instanceof StoreEntity.BackButtonEntity) {
            storeEntities.clear();
            onCategoryView = true;
            storeEntities.addAll(storeCategoriesList);
            storeEntityAdapter.notifyDataSetChanged();
        }
        else {
            cartFragment.addProductToCart((Product) entity);
        }
    }

    private void getProductCategories() {
        FoobarAPI.getCategories(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("StoreFragment", response);

                Gson gson = new Gson();
                Category[] categories = gson.fromJson(response, Category[].class);
                storeCategoriesList.clear();

                Collections.addAll(storeCategoriesList, categories);
                storeEntities.clear();
                storeEntities.addAll(storeCategoriesList);
                storeEntityAdapter.notifyDataSetChanged();
                //storeCategoryAdapter.notifyDataSetChanged();
            }
        });
    }


    private void addAllProducts(String str) {

        Gson gson = new Gson();
        Product[] products = gson.fromJson(str, Product[].class);

        Collections.addAll(storeProductList, products);

    }


    private void getProductList() {
        FoobarAPI.getProducts(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                storeProductList.clear();
                addAllProducts(response);
            }
        });
    }

    private void retrieveAccountFromCard(final String card) {
        FoobarAPI.getAccountFromCard(card, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("StringRequest", response);
                Gson gson = new Gson();
                IAccount account = gson.fromJson(response, IAccount.class);

                account.setCardId(card);
            }
        });
    }

    private void retrieveProductFromBarcode(String barcode) {

        FoobarAPI.getProductFromBarcode(barcode, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("StringRequest", response);

//                CartFragment cartFrag = (CartFragment)
//                        getChildFragmentManager().findFragmentById(R.id.store_sidebar);
//                if (cartFrag != null ) {
//                    cartFrag.addProductToCart();
//                }
//                else {
//                    throw new RuntimeException(
//                            "Removed CartFragment from layout but not redirected method" );
//                }
            }
        });
    }

    private void updateStateWithVolley() {
        final String stateTest = "{\"state\":{\"account\":{},\"products\":{\"products\":[{\"code\":\"7310500088853\",\"selected\":false,\"loading\":false,\"id\":\"e963428a-d719-422b-8d6e-5c062fe822e3\",\"name\":\"Bilys pizza\",\"qty\":1,\"price\":13,\"image\":\"http://localhost:7331/localhost:8000/media/product/7310500088853.png\"},{\"code\":\"7611612221351\",\"selected\":false,\"loading\":false,\"id\":\"4cd81a41-4e13-4b63-b833-da59dfe0faeb\",\"name\":\"Pepsi Max Ginger\",\"qty\":1,\"price\":7,\"image\":\"http://localhost:7331/localhost:8000null\"},{\"code\":\"7340083438684\",\"selected\":false,\"loading\":false,\"id\":\"7f5b3961-3654-40ad-9cea-fe0f35eb926c\",\"name\":\"Delicatoboll\",\"qty\":1,\"price\":6,\"image\":\"http://localhost:7331/localhost:8000/media/product/7340083438684.png\"}],\"page\":0,\"maxPage\":0},\"purchase\":{\"state\":\"ONGOING\"}}}";

        //TODO: make it so method require IState later on
        //FoobarAPI.sendStateToThunderpush(stateTest);
    }


    public void goToStoreTop() {
        if (!onCategoryView) {
            storeEntities.clear();
            onCategoryView = true;
            storeEntities.addAll(storeCategoriesList);
            storeEntityAdapter.notifyDataSetChanged();
        }
    }
}
