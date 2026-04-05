# TapEat Backend - F&B Self-Service Kiosk

Sistem backend untuk aplikasi kios mandiri TapEat, dibangun menggunakan Spring Boot 3 (Kotlin) dan MySQL.

---

## Order Workflow (Alur Pemesanan)

1.  **Kiosk (Pelanggan):** Membuat pesanan via `POST /api/orders`. Pesanan tersimpan dengan status **`UNPAID`** dan stok produk langsung dikurangi (reservasi).
2.  **Cashier (Kasir):** Melihat daftar pesanan masuk via `GET /api/orders/unpaid`. Setelah pembayaran diterima, Kasir memanggil `PUT /api/queue/{id}/status?status=PENDING` untuk konfirmasi.
3.  **Kitchen (Dapur):** Pesanan muncul di antrean aktif via `GET /api/queue/active`. Setelah makanan diserahkan ke pelanggan, Dapur memanggil `PUT /api/queue/{id}/status?status=DELIVERED` untuk menyelesaikan antrean.
4.  **Done List:** Semua pesanan yang sudah selesai dapat dilihat di `GET /api/queue/done`.

---

## Database & Entities

Sistem ini menggunakan tiga entitas utama untuk mengelola katalog menu dan transaksi:

- **Product**: Mengelola katalog menu dengan dukungan soft-delete (`isActive`). Jika produk dihapus, ia hanya akan ditandai sebagai tidak aktif agar riwayat pesanan lama tetap valid.
- **Order**: Mencatat detail transaksi utama seperti tipe pesanan (Dine-in/Takeaway), status pesanan, total harga, dan informasi pelanggan atau nomor meja.
- **OrderItem**: Catatan historis dari setiap item dalam pesanan. Entitas ini secara khusus mengunci harga beli (`priceAtPurchase`) saat checkout agar tidak terpengaruh oleh perubahan harga master produk di masa depan.

---

## Business Logic

- **Cashier Approval**: Setelah pesanan dibuat (`UNPAID`), kasir harus memberikan persetujuan (pindah ke `PENDING`) agar pesanan muncul di antrean dapur.
- **Stock Reservation**: Stok produk langsung dikurangi saat pesanan dibuat (`UNPAID`) untuk menjamin ketersediaan (reservasi). Jika pesanan dibatalkan (`CANCELLED`), stok otomatis dikembalikan.
- **Price Locking**: Harga produk disalin ke tabel detail pesanan saat transaksi terjadi untuk menjaga integritas data laporan keuangan.
- **Soft Delete**: Produk yang dihapus melalui API tidak akan hilang dari database, melainkan hanya diubah status `is_active` menjadi `false`.

---

## File Uploads

Manajemen file gambar menu ditangani secara lokal:

- **FileStorageService**: Secara otomatis membuat direktori `/uploads` di root proyek jika belum tersedia. Semua gambar yang diupload disimpan dengan nama unik menggunakan UUID.
- **ProductController**: Mendukung penerimaan file gambar melalui format `MultipartFile` pada request `POST` dan `PUT`.
- **WebConfig**: Mengatur pemetaan URL sehingga folder `/uploads/**` dapat diakses secara statis melalui browser atau aplikasi Android (Contoh: `http://localhost:4095/uploads/filename.jpg`).

---

## API Endpoints Details

### Products

#### Get All Active Products

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

#### Get Product by ID

`GET /api/products/{id}`

- **Response**: `200 OK` (Object Product)
- **Description**: Mengambil detail satu produk berdasarkan ID untuk keperluan edit atau detail tampilan (jika ada).

#### Create Product

`POST /api/products`

- **Request Type**: `multipart/form-data`
- **Parameters**: `name` (String), `price` (Double), `stock` (Int), `image` (File, Optional)
- **Response**: `200 OK` (Object Product)

#### Update Product

`PUT /api/products/{id}`

- **Request Type**: `multipart/form-data`
- **Parameters**: Sama dengan Create Product.
- **Response**: `200 OK` (Object Product)

#### Delete Product (Soft Delete)

`DELETE /api/products/{id}`

- **Response**: `200 OK`

---

### Orders (Transactions)

#### Get Unpaid Orders (For Cashier)

`GET /api/orders/unpaid`

- **Description**: Mengambil semua pesanan yang baru masuk dan menunggu konfirmasi/pembayaran di kasir (status `UNPAID`).
- **Response**: `200 OK` (Array of Order)

#### Get All Order History

`GET /api/orders`

- **Response**: `200 OK` (Array of Order sorted by newest)

#### Checkout Order

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
- **Response**: `200 OK` (Status awal: `UNPAID`, stok langsung dipotong/direservasi)

#### Get Order Detail

`GET /api/orders/{id}`

- **Response**: `200 OK` (Object Order)

#### Cancel Order

`PUT /api/orders/{id}/cancel`

- **Response**: `200 OK` (Status berubah jadi "CANCELLED", stok dikembalikan)

---

### Kitchen Queue

#### Get Active Queue

`GET /api/queue/active`

- **Description**: Mengambil pesanan yang sedang aktif di antrean dapur (misalnya status `PENDING` atau `COOKING`).
- **Response**: `200 OK` (Array of Order)

#### Get Completed Queue

`GET /api/queue/done`

- **Description**: Mengambil daftar pesanan yang sudah selesai diantar (status `DELIVERED`).
- **Response**: `200 OK` (Array of Order)

#### Update Order Status

`PUT /api/queue/{id}/status?status=DELIVERED`

- **Request Parameter**: `status` (String: UNPAID, PENDING, COOKING, DELIVERED, CANCELLED)
- **Response**: `200 OK` (Object Order)

---

## Menjalankan Aplikasi

1.  Pastikan MySQL berjalan dan database `TapEat` sudah dibuat.
2.  Atur kredensial di `src/main/resources/application.properties`.
3.  Jalankan dengan perintah:
    ```bash
    .\gradlew bootRun
    ```
4.  Server akan berjalan di port **4095**.

---

## Cara Build (Deployment)

Untuk melakukan build dan menghasilkan file `.jar` tanpa menjalankan unit test:

```bash
.\gradlew clean build -x test
```

File hasil build akan berada di `build/libs/`.
