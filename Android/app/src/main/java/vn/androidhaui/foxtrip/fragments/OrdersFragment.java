package vn.androidhaui.foxtrip.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.androidhaui.foxtrip.R;
import vn.androidhaui.foxtrip.adapters.OrdersAdapter;
import vn.androidhaui.foxtrip.databinding.FragmentOrdersBinding;
import vn.androidhaui.foxtrip.viewmodels.OrdersViewModel;

public class OrdersFragment extends Fragment {
    private FragmentOrdersBinding binding;
    private OrdersViewModel vm;
    private OrdersAdapter adapter;

    public OrdersFragment() {}

    public static OrdersFragment newInstance() { return new OrdersFragment(); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(OrdersViewModel.class);

        adapter = new OrdersAdapter(order -> {
            // mở chi tiết
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, OrderDetailFragment.newInstance(order.getId()))
                    .addToBackStack(null)
                    .commit();
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);

        vm.getOrders().observe(getViewLifecycleOwner(), orders -> adapter.setItems(orders));
        vm.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                binding.tvEmpty.setText(msg);
                vm.clearMessage();
            }
        });
        vm.isLoading().observe(getViewLifecycleOwner(), loading -> binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE));

        // load
        vm.loadOrders();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
