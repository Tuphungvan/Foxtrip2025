package vn.androidhaui.foxtrip.network;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import vn.androidhaui.foxtrip.models.AdminOrderResponse;
import vn.androidhaui.foxtrip.models.CartData;
import vn.androidhaui.foxtrip.models.CheckoutData;
import vn.androidhaui.foxtrip.models.CountData;
import vn.androidhaui.foxtrip.models.History;
import vn.androidhaui.foxtrip.models.Order;
import vn.androidhaui.foxtrip.models.OrderResponse;
import vn.androidhaui.foxtrip.models.PaymentData;
import vn.androidhaui.foxtrip.models.PlaceOrderRequest;
import vn.androidhaui.foxtrip.models.RevenueReport;
import vn.androidhaui.foxtrip.models.SearchOrderResponse;
import vn.androidhaui.foxtrip.models.SendOTPRequest;
import vn.androidhaui.foxtrip.models.Tour;
import vn.androidhaui.foxtrip.models.User;
import vn.androidhaui.foxtrip.models.VerifyOTPRequest;

public interface ApiService {

    @GET("/search")
    Call<ApiResponse<List<Tour>>> searchTours(
            @Query("q") String q,
            @Query("province") String province,
            @Query("category") String category,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("priceMin") String priceMin,
            @Query("priceMax") String priceMax
    );

    @GET("/region/bac")
    Call<ApiResponse<List<Tour>>> getToursBac();

    @GET("/region/trung")
    Call<ApiResponse<List<Tour>>> getToursTrung();

    @GET("/region/nam")
    Call<ApiResponse<List<Tour>>> getToursNam();

    @GET("/hot")
    Call<ApiResponse<List<Tour>>> getHotTours();

    @GET("/discount")
    Call<ApiResponse<List<Tour>>> getDiscountTours();

    @GET("/tours/{slug}")
    Call<ApiResponse<Tour>> getTourDetail(@Path("slug") String slug);

    // ===== AUTH =====
    @POST("/auth/register")
    Call<ApiResponse<Map<String, Object>>> register(@Body Map<String, String> body);

    @POST("/auth/login")
    Call<ApiResponse<Map<String, Object>>> login(@Body Map<String, String> body);

    @POST("/auth/login/google")
    Call<ApiResponse<Map<String, Object>>> loginWithGoogle(@Body Map<String, String> body);

    @POST("/auth/login/facebook")
    Call<ApiResponse<Map<String, Object>>> loginWithFacebook(@Body Map<String, String> body);

    @POST("/auth/logout")
    Call<ApiResponse<Void>> logout();

    @POST("/chatbot/chat")
    Call<ApiResponse<String>> sendChatMessage(@Body Map<String, String> body);

    @GET("/auth/check-login-status")
    Call<ApiResponse<Map<String, Object>>> checkLoginStatus();

    @GET("/tour/shorts")
    Call<ApiResponse<Tour>> getRandomShort();

    @POST("/cart/add/{slug}")
    Call<ApiResponse<Void>> addToCart(@Path("slug") String slug, @Body Map<String, Object> body);

    @GET("/cart")
    Call<ApiResponse<CartData>> getCart();

    @DELETE("/cart/{slug}")
    Call<ApiResponse<CartData>> removeFromCart(@Path("slug") String slug);

    @POST("/cart/decrease/{slug}")
    Call<ApiResponse<CartData>> decreaseQuantity(@Path("slug") String slug);

    @POST("/cart/increase/{slug}")
    Call<ApiResponse<CartData>> increaseQuantity(@Path("slug") String slug);

    @GET("/cart/count")
    Call<ApiResponse<CountData>> getCartCount();

    @GET("/checkout")
    Call<ApiResponse<CheckoutData>> getCheckout();

    @POST("/checkout/place-order")
    Call<ApiResponse<OrderResponse>> placeOrder(@Body PlaceOrderRequest body);

    @GET("/checkout/payment/{id}")
    Call<ApiResponse<PaymentData>> getPaymentInfo(@Path("id") String orderId);

    //otp
    @POST("otp/send")
    Call<ApiResponse<Void>> sendOTP(@Body SendOTPRequest request);

    @POST("otp/verify")
    Call<ApiResponse<Void>> verifyOTP(@Body VerifyOTPRequest request);

    // Profile
    @GET("/profile")
    Call<ApiResponse<Map<String, Object>>> getProfile();

    @GET("/profile/my-orders")
    Call<ApiResponse<List<Order>>> getMyOrders();

    @GET("/profile/my-orders/{orderId}")
    Call<ApiResponse<Order>> getOrderDetail(@Path("orderId") String orderId);

    @GET("/profile/history")
    Call<ApiResponse<List<History>>> getHistory();

    @GET("/profile/history/{historyId}")
    Call<ApiResponse<History>> getHistoryDetail(@Path("historyId") String historyId);

    @GET("/profile/update-profile")
    Call<ApiResponse<Map<String, Object>>> getUpdateProfile();

    @POST("/profile/update-profile")
    Call<ApiResponse<Map<String, Object>>> postUpdateProfile(@Body Map<String, Object> body);

    @GET("/profile/recharge-wallet")
    Call<ApiResponse<Map<String, Object>>> getWallet();

    @POST("/profile/recharge-wallet")
    Call<ApiResponse<Map<String, Object>>> postRechargeWallet(@Body Map<String, Object> body);

    // Thêm vào ApiService.java trong phần Profile

    @DELETE("/profile/my-orders/{orderId}/cancel")
    Call<ApiResponse<Map<String, Object>>> cancelOrder(@Path("orderId") String orderId);

    @POST("/profile/my-orders/{orderId}/pay")
    Call<ApiResponse<Map<String, Object>>> payOrder(@Path("orderId") String orderId);

    // Avatar upload (multipart)
    @Multipart
    @POST("/profile/avatar")
    Call<ApiResponse<Map<String, Object>>> uploadAvatar(@Part MultipartBody.Part avatar);

    @GET("/admin/overview")
    Call<ApiResponse<Map<String, Object>>> getAdminOverview();

    @GET("/admin/revenue")
    Call<ApiResponse<List<RevenueReport>>> getAdminRevenue();

    // Admin tours
    @GET("/admin/tours")
    Call<ApiResponse<List<Tour>>> getAdminTours();

    @POST("/admin/tours")
    Call<ApiResponse<Tour>> createAdminTour(@Body Map<String, Object> body);

    @PUT("/admin/tours/{id}")
    Call<ApiResponse<Tour>> updateAdminTour(@Path("id") String id, @Body Map<String, Object> body);

    @DELETE("/admin/tours/{id}")
    Call<ApiResponse<Void>> deleteAdminTour(@Path("id") String id);

    @Multipart
    @POST("/admin/upload-images")
    Call<ApiResponse<List<String>>> uploadImages(
            @Part List<MultipartBody.Part> images,
            @Part("tourName") RequestBody tourName
    );

    @GET("/admin/users")
    Call<ApiResponse<List<User>>> getAdminUsers(@Query("search") String search, @Query("sort") String sort);

    @POST("/admin/users/{id}/deactivate")
    Call<ApiResponse<User>> deactivateAdminUser(@Path("id") String id);

    @POST("/admin/users/{id}/activate")
    Call<ApiResponse<User>> activateAdminUser(@Path("id") String id);

    @POST("/admin/users/{id}/reset-password")
    Call<ApiResponse<User>> resetAdminUserPassword(@Path("id") String id);

    @POST("/admin/create-admin")
    Call<Map<String, Object>> createAdmin(@Body Map<String, String> body);

    // Orders - Admin
    @GET("/admin/manager-order/pending-payment")
    Call<ApiResponse<List<Order>>> getOrdersPendingPayment();

    @GET("/admin/manager-order/to-confirm")
    Call<ApiResponse<List<Order>>> getOrdersToConfirm();

    @GET("/admin/manager-order/completed")
    Call<ApiResponse<List<Order>>> getOrdersCompleted();

    //tìm order

    @GET("/admin/manager-order/search")
    Call<ApiResponse<SearchOrderResponse>> searchOrderById(@Query("q") String query);

    //admin order detail
    @GET("/admin/manager-order/{orderId}")
    Call<ApiResponse<Order>> getAdminOrderDetail(@Path("orderId") String orderId);

    @DELETE("/admin/manager-order/delete/{orderId}")
    Call<ApiResponse<AdminOrderResponse>> deletePendingOrder(@Path("orderId") String orderId);

    @POST("/admin/manager-order/confirm/{orderId}")
    Call<ApiResponse<AdminOrderResponse>> confirmOrder(@Path("orderId") String orderId);

    @POST("/admin/manager-order/complete/{orderId}")
    Call<ApiResponse<AdminOrderResponse>> completeExpiredOrder(@Path("orderId") String orderId);

}
