package vn.androidhaui.foxtrip.repositories;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.network.ApiClient;
import vn.androidhaui.foxtrip.network.ApiResponse;
import vn.androidhaui.foxtrip.network.ApiService;

public class ExploreRepository {

    public interface OnResult {
        void onSuccess(List<Tour> tours);
        void onError(Throwable t);
    }

    private final ApiService api;

    public ExploreRepository(Context context, String baseUrl) {
        Context ctx = context.getApplicationContext();
        this.api = ApiClient.getClient(ctx, baseUrl).create(ApiService.class);
    }

    private String formatDate(Long millis) {
        if (millis == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    public void searchTours(String q,
                            List<String> provinces,
                            List<String> categories,
                            Long startDateMs,
                            Long endDateMs,
                            String priceMin,
                            String priceMax,
                            String sortOption,
                            @NonNull OnResult cb) {

        String provinceParam = null;
        String categoryParam = null;

        if (provinces != null && provinces.size() == 1) provinceParam = provinces.get(0);
        if (categories != null && categories.size() == 1 && !"Tất cả".equals(categories.get(0))) categoryParam = categories.get(0);

        String startDate = formatDate(startDateMs);
        String endDate = formatDate(endDateMs);

        Call<ApiResponse<List<Tour>>> call = api.searchTours(
                (q == null || q.trim().isEmpty()) ? null : q.trim(),
                provinceParam,
                categoryParam,
                startDate,
                endDate,
                priceMin,
                priceMax
        );

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Tour>>> call, @NonNull Response<ApiResponse<List<Tour>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Tour> list = response.body().getData();
                    if (list == null) list = new ArrayList<>();

                    List<Tour> filtered = new ArrayList<>();

                    for (Tour t : list) {
                        boolean ok = true;

                        if (provinces != null && !provinces.isEmpty()) {
                            if (provinces.size() > 1) {
                                ok = provinces.contains(t.getProvince());
                            }
                        }

                        if (ok && categories != null && !categories.isEmpty()) {
                            if (categories.size() > 1) {
                                if (!"Tất cả".equals(categories.get(0))) {
                                    ok = categories.contains(t.getCategory());
                                }
                            }
                        }

                        if (ok && ( (priceMin != null && !priceMin.isEmpty()) || (priceMax != null && !priceMax.isEmpty()) )) {
                            double p = (t.getPrice() == null ? 0.0 : t.getPrice()) * (1 - (t.getDiscount() == null ? 0.0 : t.getDiscount()) / 100.0);
                            if (!TextUtils.isEmpty(priceMin)) {
                                try {
                                    double min = Double.parseDouble(priceMin);
                                    if (p < min) ok = false;
                                } catch (NumberFormatException ignored) {}
                            }
                            if (ok && !TextUtils.isEmpty(priceMax)) {
                                try {
                                    double max = Double.parseDouble(priceMax);
                                    if (p > max) ok = false;
                                } catch (NumberFormatException ignored) {}
                            }
                        }

                        if (ok) filtered.add(t);
                    }

                    if (sortOption != null) {
                        if ("Giá tăng dần".equals(sortOption)) {
                            filtered.sort(Comparator.comparingDouble(o -> {
                                double price = o.getPrice() == null ? 0.0 : o.getPrice();
                                double discount = o.getDiscount() == null ? 0.0 : o.getDiscount();
                                return price * (1 - discount / 100.0);
                            }));                        } else if ("Giá giảm dần".equals(sortOption)) {
                            filtered.sort((o1, o2) -> {
                                double p1 = o1.getPrice() == null ? 0.0 : o1.getPrice();
                                double p2 = o2.getPrice() == null ? 0.0 : o2.getPrice();
                                return Double.compare(p2, p1);
                            });
                        }
                    }

                    cb.onSuccess(filtered);
                } else {
                    cb.onError(new Exception("API lỗi: " + (response.code())));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Tour>>> call, @NonNull Throwable t) {
                Log.e("ExploreRepo", "onFailure", t);
                cb.onError(t);
            }
        });
    }
}
