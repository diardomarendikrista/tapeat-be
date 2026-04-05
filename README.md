# TapEat Backend - F&B Self-Service Kiosk

Sistem backend untuk aplikasi kios mandiri TapEat, dibangun menggunakan Spring Boot 3 (Kotlin) dan MySQL.

<hr/>

## 1. Database & Entities

Sistem ini menggunakan tiga entitas utama untuk mengelola katalog menu dan transaksi:

- **Product**: Mengelola katalog menu dengan dukungan soft-delete (`isActive`). Jika produk dihapus, ia hanya akan ditandai sebagai tidak aktif agar riwayat pesanan lama tetap valid.
- **Order**: Mencatat detail transaksi utama seperti tipe pesanan (Dine-in/Takeaway), status pesanan, total harga, dan informasi pelanggan atau nomor meja.
- **OrderItem**: Catatan historis dari setiap item dalam pesanan. Entitas ini secara khusus mengunci harga beli (`priceAtPurchase`) saat checkout agar tidak terpengaruh oleh perubahan harga master produk di masa depan.

<hr/>

## 2. CORE Business Logic (Transactional)

Logika bisnis utama diimplementasikan di dalam `OrderService` dan `ProductService`:

- **Stock Reduction**: Saat pesanan dibuat, sistem akan mengecek ketersediaan stok produk. Jika stok mencukupi, sistem akan mengurangi stok secara otomatis sebelum menyimpan pesanan.
- **Price Locking**: Harga produk disalin ke tabel detail pesanan saat transaksi terjadi untuk menjaga integritas data laporan keuangan.
- **Order Cancellation**: Jika pesanan dibatalkan (status berubah menjadi `CANCELLED`), sistem akan melakukan pengulangan pada setiap item pesanan dan mengembalikan jumlah stok ke master produk.
- **Soft Delete**: Produk yang dihapus melalui API tidak akan hilang dari database, melainkan hanya diubah status `is_active` menjadi `false`.

<hr/>

## 3. File Uploads

Manajemen file gambar menu ditangani secara lokal:

- **FileStorageService**: Secara otomatis membuat direktori `/uploads` di root proyek jika belum tersedia. Semua gambar yang diupload disimpan dengan nama unik menggunakan UUID.
- **ProductController**: Mendukung penerimaan file gambar melalui format `MultipartFile` pada request `POST` dan `PUT`.
- **WebConfig**: Mengatur pemetaan URL sehingga folder `/uploads/**` dapat diakses secara statis melalui browser atau aplikasi Android (Contoh: `http://localhost:4095/uploads/filename.jpg`).

<hr/>

## API Endpoints Details

### A. Products (Admin & Catalog)

#### 1. Get All Active Products

`GET /api/products`

- **Response**: `200 OK` (Array of Product)
- **Contoh Response**:
  ```json
  [
    {
      "id": 1,
      "name": "Kopi Susu",
      "price": 15000,
      "stock": 50,
      "imageUrl": "uploads/uuid-kopi.jpg",
      "isActive": true
    }
  ]
  ```

#### 2. Create Product

`POST /api/products`

- **Request Type**: `multipart/form-data`
- **Parameters**:
  - `name` (String)
  - `price` (Double)
  - `stock` (Int)
  - `image` (File, Optional)
- **Response**: `200 OK` (Object Product)

#### 3. Update Product

`PUT /api/products/{id}`

- **Request Type**: `multipart/form-data`
- **Parameters**: Sama dengan Create Product.
- **Response**: `200 OK` (Object Product)

#### 4. Delete Product (Soft Delete)

`DELETE /api/products/{id}`

- **Response**: `200 OK`

<hr/>

### B. Orders (Transactions)

#### 1. Get All Order History

`GET /api/orders`

- **Response**: `200 OK` (Array of Order sorted by newest)

#### 2. Checkout Order

`POST /api/orders`

- **Request Body** (JSON):
  ```json
  {
    "orderType": "DINE_IN",
    "tableNumber": "05",
    "customerName": null,
    "items": [
      { "productId": 1, "quantity": 2 },
      { "productId": 3, "quantity": 1 }
    ]
  }
  ```
- **Response**: `200 OK` (Object Order lengkap dengan items)

#### 2. Get Order Detail

`GET /api/orders/{id}`

- **Response**: `200 OK` (Object Order)

#### 3. Cancel Order

`PUT /api/orders/{id}/cancel`

- **Response**: `200 OK` (Status berubah jadi "CANCELLED", stok dikembalikan)

<hr/>

### C. Kitchen (Queue Management)

#### 1. Get Active Queue

`GET /api/queue/active`

- **Description**: Mengambil pesanan dengan status `PENDING` atau `COOKING`.
- **Response**: `200 OK` (Array of Order)

#### 2. Update Order Status

`PUT /api/queue/{id}/status?status=READY`

- **Request Parameter**: `status` (String: PENDING, COOKING, READY)
- **Response**: `200 OK` (Object Order)

<hr/>

## Menjalankan Aplikasi

1. Pastikan MySQL berjalan dan database `TapEat` sudah dibuat.
2. Atur kredensial di `src/main/resources/application.properties`.
3. Jalankan dengan perintah:
   ```bash
   .\gradlew bootRun
   ```
4. Server akan berjalan di port **4095**.

<hr/>

## Cara Build (Deployment)
Untuk melakukan build dan menghasilkan file `.jar` tanpa menjalankan unit test:
```bash
.\gradlew clean build -x test
```
File hasil build akan berada di `build/libs/`.
