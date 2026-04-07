# TapEat Backend - F&B Self-Service Kiosk

Sistem backend untuk aplikasi kios mandiri TapEat, dibangun menggunakan Spring Boot 3 (Kotlin) dan MySQL.

---

## Pembagian Tugas

Berikut adalah kontributor dan pembagian tanggung jawab dalam pengembangan backend TapEat:

1.  **Diardo Marendi Krista**

    - **Init Project & Boilerplate**: Inisialisasi Spring Boot, konfigurasi JPA/Hibernate, dan arsitektur dasar.
    - **Order Module**: Implementasi logika checkout, manajemen status pesanan (UNPAID/CANCELLED), dan reservasi stok.
    - **Database Schema**: Perancangan ERD dan struktur tabel MySQL.

2.  **Zhafira Zila Qonita**

    - **Product Module**: Implementasi CRUD produk, logika filtering produk (availableOnly), dan soft delete.
    - **File Storage**: Manajemen upload gambar menu ke direktori server menggunakan `FileStorageService`.
    - **Config**: Pengaturan static resource mapping untuk akses file. (WebConfig.kt)

3.  **Yohanes Septian Prasetyo**
    - **Queue Module**: Implementasi antrean dapur (PENDING/DELIVERED) dan riwayat penyelesaian.
    - **Documentation**: Pembuatan README dan dokumentasi teknis API Endpoints.
    - **Testing & Quality**: Validasi endpoint dan integrasi logika antar modul.

---

## Alur & Logika Bisnis

Sistem ini mengelola siklus pesanan mulai dari pemilihan menu hingga penyajian dengan aturan main sebagai berikut:

### Kiosk (Pelanggan)

- **Melihat Menu**: Menggunakan `GET /api/products?availableOnly=true`. Bisa ditambah filter `&category=...` atau `&name=...`. Produk stok 0 otomatis disembunyikan.
- **Checkout Pesanan**: Mengirim data via `POST /api/orders`. Status awal adalah `UNPAID`.
- **Reservasi Stok**: Saat checkout berhasil, sistem otomatis memotong stok produk agar tidak "balapan" dengan pelanggan lain.
- **Penguncian Harga**: Harga produk saat itu langsung dikunci di tabel detail pesanan (`priceAtPurchase`) agar riwayat keuangan tetap akurat meskipun harga master produk berubah.

### Cashier (Kasir)

- **Melihat Pesanan Masuk**: Mengambil daftar pesanan yang belum bayar via `GET /api/orders/unpaid`.
- **Konfirmasi Pembayaran**: Setelah uang diterima, kasir memanggil `PUT /api/orders/{id}/status?status=PENDING`. Pesanan akan berpindah dari antrean kasir ke antrean dapur.
- **Pembatalan Pesanan**: Jika pelanggan batal bayar/checkout, kasir memanggil `PUT /api/orders/{id}/status?status=CANCELLED`.
- **Restorasi Stok**: Saat status diubah menjadi `CANCELLED`, sistem otomatis mengembalikan jumlah stok yang tadi terpotong ke database produk secara _real-time_.

### Kitchen (Dapur)

- **Melihat Antrean Masak**: Mengambil daftar pesanan aktif via `GET /api/queue/active` (Status `PENDING` atau `COOKING`).
- **Update Status Masak**: Dapur memanggil `PUT /api/queue/{id}/status?status=COOKING` saat mulai memasak, dan status `DELIVERED` jika makanan sudah diserahkan.
- **Riwayat Pesanan Selesai**: Melihat daftar pesanan yang sudah beres via `GET /api/queue/done`.

### Admin (Manajemen)

- **Manajemen Menu**: Menggunakan `GET /api/products` (default `availableOnly=false`) untuk melihat semua produk termasuk yang stoknya 0 untuk keperluan restock.
- **Operasi CRUD**: Mengelola katalog via `POST /api/products` (Tambah), `PUT /api/products/{id}` (Edit), dan `DELETE /api/products/{id}` (Hapus).
- **Soft Delete**: Penghapusan produk hanya mengubah status `isActive` menjadi `false` demi menjaga integritas data riwayat transaksi.
- **Monitoring Transaksi**: Melihat seluruh riwayat transaksi yang pernah terjadi via `GET /api/orders`.

---

## File Uploads

Manajemen file gambar menu ditangani secara lokal:

- **FileStorageService**: Secara otomatis membuat direktori `/uploads` di root proyek jika belum tersedia. Semua gambar yang diupload disimpan dengan nama unik menggunakan UUID.
- **ProductController**: Mendukung penerimaan file gambar melalui format `MultipartFile` pada request `POST` dan `PUT`.
- **WebConfig**: Mengatur pemetaan URL sehingga folder `/uploads/**` dapat diakses secara statis melalui browser atau aplikasi Android (Contoh: `http://localhost:4095/uploads/filename.jpg`).

---

## API Endpoints Details

### Products

#### Get Products (Kiosk & Admin)

`GET /api/products`

- **Description**: Mengambil daftar produk aktif. Mendukung filter untuk kebutuhan Kiosk maupun Admin.
- **Query Parameters**:
  - `name` (String, Optional): Mencari produk berdasarkan nama.
  - `category` (String, Optional): Memfilter produk berdasarkan kategori (misal: `Makanan`, `Minuman`).
  - `availableOnly` (Boolean, Default: `false`): Jika `true`, hanya menampilkan produk dengan stok > 0 (untuk Kiosk). Jika `false`, menampilkan semua produk aktif termasuk yang stoknya habis (untuk Admin).
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

#### Update Order Status

`PUT /api/orders/{id}/status?status=PENDING`

- **Description**: Digunakan oleh Kasir untuk konfirmasi pembayaran (`PENDING`) atau pembatalan (`CANCELLED`).
- **Request Parameter**: `status` (String: PENDING, CANCELLED)
- **Response**: `200 OK` (Object Order)

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
