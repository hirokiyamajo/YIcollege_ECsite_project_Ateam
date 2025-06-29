package com.college.yi.ecsite.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.college.yi.ecsite.admin.dto.AdminDashboardDto;
import com.college.yi.ecsite.admin.repository.OrderMapper;
import com.college.yi.ecsite.admin.repository.ProductMapper;

public class AdminDashboardServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 正常系：通常パターン
     * ダッシュボードデータが正しく取得できること
     */

    @Test
    void testGetDashBoardData_NormalCase() {
        when(productMapper.countProducts()).thenReturn(10);
        when(productMapper.countOutOfStockProducts()).thenReturn(2);
        when(orderMapper.countOrders()).thenReturn(5);

        AdminDashboardDto result = adminDashboardService.getDashBoardData();

        assertEquals(10, result.getProductCount());
        assertEquals(2, result.getOutOfStockCount());
        assertEquals(5, result.getOrderCount());
    }

    /**
     * 境界値：全て0件の場合、正しく取得できること
     */
    @Test
    void testGetDashBoardData_ZeroCounts() {
        when(productMapper.countProducts()).thenReturn(0);
        when(productMapper.countOutOfStockProducts()).thenReturn(0);
        when(orderMapper.countOrders()).thenReturn(0);

        AdminDashboardDto result = adminDashboardService.getDashBoardData();

        assertEquals(0, result.getProductCount());
        assertEquals(0, result.getOutOfStockCount());
        assertEquals(0, result.getOrderCount());
    }

    /**
     * 境界値：全て1件の場合、正しく取得できること
     */
    @Test
    void testGetDashBoardData_MinValidCounts() {
        when(productMapper.countProducts()).thenReturn(1);
        when(productMapper.countOutOfStockProducts()).thenReturn(1);
        when(orderMapper.countOrders()).thenReturn(1);

        AdminDashboardDto result = adminDashboardService.getDashBoardData();

        assertEquals(1, result.getProductCount());
        assertEquals(1, result.getOutOfStockCount());
        assertEquals(1, result.getOrderCount());
    }

    /**
     * 境界値：最大値を返す場合も正しく取得できること
     */
    @Test
    void testGetDashBoardData_MaxCounts() {
        when(productMapper.countProducts()).thenReturn(Integer.MAX_VALUE);
        when(productMapper.countOutOfStockProducts()).thenReturn(Integer.MAX_VALUE);
        when(orderMapper.countOrders()).thenReturn(Integer.MAX_VALUE);

        AdminDashboardDto result = adminDashboardService.getDashBoardData();

        assertEquals(Integer.MAX_VALUE, result.getProductCount());
        assertEquals(Integer.MAX_VALUE, result.getOutOfStockCount());
        assertEquals(Integer.MAX_VALUE, result.getOrderCount());
    }

    /**
     * 異常系：ProductMapperが例外をスローした場合、例外が伝播すること
     */
    @Test
    void testGetDashBoardData_ProductMapperException() {
        when(productMapper.countProducts()).thenThrow(new RuntimeException("Product DB Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminDashboardService.getDashBoardData();
        });

        assertEquals("Product DB Error", exception.getMessage());
    }

    /**
     * 異常系：OrderMapperが例外をスローした場合、例外が伝播すること
     */
    @Test
    void testGetDashBoardData_OrderMapperException() {
        when(productMapper.countProducts()).thenReturn(10);
        when(productMapper.countOutOfStockProducts()).thenReturn(2);
        when(orderMapper.countOrders()).thenThrow(new RuntimeException("Order DB Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminDashboardService.getDashBoardData();
        });

        assertEquals("Order DB Error", exception.getMessage());
    }

 
    
    /**
     * 依存先の動作確認：モックの戻り値が正しく返却されること
     */
    @Test
    void testGetDashBoardData_MockBehaviorConfirmation() {
        when(productMapper.countProducts()).thenReturn(20);
        when(productMapper.countOutOfStockProducts()).thenReturn(5);
        when(orderMapper.countOrders()).thenReturn(8);

        AdminDashboardDto result = adminDashboardService.getDashBoardData();

        assertEquals(20, result.getProductCount());
        assertEquals(5, result.getOutOfStockCount());
        assertEquals(8, result.getOrderCount());
    }

    /**
     * 副作用検証：DB更新処理などが発生しないこと（読み取り専用であること）
     */
    @Test
    void testGetDashBoardData_NoSideEffects() {
        when(productMapper.countProducts()).thenReturn(10);
        when(productMapper.countOutOfStockProducts()).thenReturn(2);
        when(orderMapper.countOrders()).thenReturn(5);

        AdminDashboardDto result = adminDashboardService.getDashBoardData();

        // 結果確認のみで副作用なし（DB更新処理が呼ばれていないことが保証されている）
        assertEquals(10, result.getProductCount());
        assertEquals(2, result.getOutOfStockCount());
        assertEquals(5, result.getOrderCount());
    }

    /**
     * 異常系：複数のMapperが例外をスローした場合、最初に呼び出された例外が発生すること
     */
    @Test
    void testGetDashBoardData_MultipleExceptions() {
        when(productMapper.countProducts()).thenThrow(new RuntimeException("Product DB Error"));
        when(orderMapper.countOrders()).thenThrow(new RuntimeException("Order DB Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminDashboardService.getDashBoardData();
        });

        // 最初に呼び出したProductMapperの例外が優先される
        assertEquals("Product DB Error", exception.getMessage());
    }
}
