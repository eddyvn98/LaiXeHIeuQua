# Đặc tả ứng dụng Lái Xe Hiệu Quả

**Phiên bản:** v0.1  
**Trạng thái:** Discussion Draft  
**Nền tảng mục tiêu:** Android 13  
**Loại ứng dụng:** Android APK  
**Đối tượng ban đầu:** Xe máy, điện thoại gắn cố định trên xe  
**Ngày cập nhật:** 2026-09-28

---

## 1. Mục tiêu sản phẩm

Ứng dụng không chỉ ghi lại hành trình GPS mà đóng vai trò như một **trợ lý lái xe realtime**, giúp người dùng:

- Theo dõi liên tục quãng đường, vận tốc, gia tốc, độ mượt, góc nghiêng và trạng thái chuyển động.
- Phát hiện hành vi lái bất thường hoặc kém hiệu quả ngay khi đang chạy.
- Cảnh báo realtime để người dùng điều chỉnh ga hoặc cách vận hành.
- Học từ lịch sử của chính người dùng thay vì chỉ dùng một bộ ngưỡng cố định.
- Chọn **Best Efficient Session** làm hệ quy chiếu.
- So sánh chuyến hiện tại với Best Session dưới dạng "ghost".
- Dự đoán kết quả cuối chuyến nếu người dùng tiếp tục giữ cách chạy hiện tại.
- Theo dõi các lần đổ xăng và dần ước tính mức tiêu hao thực tế.
- Dự đoán số km còn có thể chạy trước khi hết xăng.
- Tối giản giao diện khi lái xe; chỉ hiển thị thông tin cần thiết đúng thời điểm.

---

## 2. Triết lý sản phẩm

Ứng dụng được định hướng như một **dashboard/hộp đen/trợ lý lái xe**, không phải một ứng dụng thống kê GPS thông thường.

Chuỗi xử lý cốt lõi:

```
Sensors
  ↓
Hiểu trạng thái lái
  ↓
Đánh giá hiệu quả tức thời
  ↓
So với Best Session / lịch sử
  ↓
Phát hiện lệch hoặc bất thường
  ↓
Cảnh báo realtime
  ↓
Dự đoán nếu tiếp tục cách chạy hiện tại
```

Nguyên tắc UI:

> Không hiển thị mọi thứ chỉ vì app đo được mọi thứ.

Thông tin kỹ thuật vẫn được lưu để phân tích nhưng không nhất thiết hiển thị khi đang lái.

---

## 3. Phạm vi v0.1

### Có trong phạm vi

- Android 13 APK.
- Chạy offline.
- Start/Stop một phiên lái xe.
- Tracking tiếp tục khi tắt màn hình.
- GPS/GNSS.
- Vận tốc.
- Quãng đường.
- Thời gian.
- Accelerometer.
- Gyroscope.
- Rotation Vector.
- Gia tốc theo hướng chuyển động.
- Độ mượt khi tăng/giảm tốc.
- Góc nghiêng xe khi điện thoại được gắn cố định.
- Phát hiện hard acceleration.
- Phát hiện hard braking.
- Phát hiện dao động vận tốc/ga.
- Realtime efficiency.
- Realtime alert.
- Session history.
- Best Efficient Session.
- So sánh Current với Best.
- Ghost chart.
- Future projection.
- Fuel entries.
- Full-tank cycle.
- km/L ước tính.
- Estimated remaining range.

### Chưa cần trong v0.1

- Cloud account.
- Server backend.
- Đồng bộ nhiều thiết bị.
- AI/ML phức tạp.
- OBD Bluetooth.
- Bản đồ heatmap nâng cao.
- Phân tích camera.
- Social/ranking với người dùng khác.

---

## 4. Điều kiện sử dụng cảm biến

Để đo góc nghiêng và gia tốc theo đúng hệ trục của xe:

- Điện thoại cần được **gắn cố định trên xe**.
- Vị trí điện thoại không nên thay đổi trong một Session.
- Trước khi bắt đầu Session, app thực hiện calibration.
- Khi calibration, xe được giữ ở tư thế thẳng và tư thế đó được lấy làm mốc 0°.

Nếu điện thoại không cố định, các metric liên quan đến:

- lean angle,
- longitudinal acceleration,
- lateral acceleration,

có thể không đáng tin cậy.

---

## 5. Luồng sử dụng chính

### 5.1. Trước chuyến đi

Người dùng mở app.

Màn hình chính hiển thị tối giản:

- trạng thái xe,
- range ước tính,
- nút **Start Driving**,
- Best/reference route nếu nhận diện được.

Người dùng bấm Start.

App:

1. Kiểm tra quyền Location.
2. Khởi động Foreground Service.
3. Kiểm tra GNSS.
4. Calibration cảm biến.
5. Tạo Session mới.
6. Bắt đầu ghi dữ liệu.

---

### 5.2. Trong khi lái

Ứng dụng:

- ghi dữ liệu liên tục,
- phân tích cửa sổ thời gian ngắn,
- cập nhật efficiency,
- so Current với Best Session khi có reference,
- cảnh báo khi phát hiện bất thường có thể điều chỉnh,
- cập nhật prediction.

Người dùng không cần thao tác liên tục.

---

### 5.3. Kết thúc chuyến

Người dùng Stop hoặc Session tự kết thúc trong tương lai.

App tạo Trip Summary gồm:

- distance,
- duration,
- average speed,
- max speed,
- efficiency,
- smooth acceleration,
- smooth braking,
- stopped time,
- hard acceleration count,
- hard braking count,
- lean max left/right,
- estimated fuel used,
- estimated remaining range,
- comparison với Best/reference.

---

## 6. Dữ liệu cần thu thập

### 6.1. GPS/GNSS

Mỗi Track Point dự kiến lưu:

- timestamp,
- latitude,
- longitude,
- altitude nếu có,
- location accuracy,
- GNSS speed,
- speed accuracy nếu có,
- bearing,
- bearing accuracy nếu có.

Tần suất ban đầu dự kiến:

- khoảng 1 Hz cho GPS/GNSS.

---

### 6.2. Motion sensors

Nguồn:

- Accelerometer,
- Gyroscope,
- Rotation Vector,
- Linear Acceleration nếu thiết bị hỗ trợ phù hợp.

Tần suất ban đầu:

- khoảng 10–25 Hz.

Không cần sampling quá cao trong v0.1.

Dữ liệu xử lý cần suy ra:

- longitudinal acceleration,
- lateral acceleration,
- vertical disturbance,
- jerk,
- lean angle,
- pitch,
- movement stability.

---

## 7. Sensor Fusion

Không đánh giá hành vi chỉ dựa trên accelerometer thô do xe máy có rung và ổ gà.

Nguồn cần kết hợp:

```
GNSS speed
+
Linear acceleration / accelerometer
+
Gyroscope
+
Rotation vector
```

Ví dụ:

Nếu accelerometer báo gia tốc lớn nhưng GNSS speed không thay đổi đáng kể:

- có thể là rung,
- ổ gà,
- dịch chuyển điện thoại,
- không nên tự động kết luận hard acceleration.

Nếu GNSS speed tăng nhanh và sensor đồng thời xác nhận acceleration dương:

- độ tin cậy hard acceleration cao hơn.

---

## 8. Driving Efficiency

### 8.1. Phân biệt hai khái niệm

**Driving Efficiency**

Có thể đánh giá realtime bằng cảm biến.

**Fuel Efficiency**

Không thể biết chính xác chỉ bằng điện thoại nếu không có dữ liệu trực tiếp từ ECU/OBD hoặc cảm biến mức nhiên liệu.

Fuel Efficiency trong v0.1 là giá trị **ước tính/học từ các lần đổ xăng**.

---

### 8.2. Driving Efficiency có thể xem xét

- độ mượt khi tăng tốc,
- độ mượt khi giảm tốc,
- hard acceleration,
- hard braking,
- jerk,
- speed oscillation,
- stop & go,
- thời gian đứng yên,
- mức ổn định của vận tốc,
- biến động chuyển động trong một cửa sổ thời gian.

Góc nghiêng không mặc định bị trừ điểm vì vào cua hợp lý không đồng nghĩa với lái kém hiệu quả.

---

## 9. Realtime Efficiency

Ứng dụng cần có một chỉ số hiệu quả tức thời dựa trên cửa sổ gần nhất, ví dụ:

- 10 giây,
- 20 giây,
- hoặc 30 giây.

Ví dụ:

```
Chạy mượt              92
Ga bắt đầu dao động    78
Tăng tốc mạnh          55
Phanh/tăng liên tục    42
Ổn định trở lại        85
```

Realtime Efficiency khác với tổng điểm toàn Session.

Mục tiêu của realtime score là giúp người dùng điều chỉnh ngay khi lái.

---

## 10. Phát hiện bất thường realtime

Không sử dụng duy nhất một ngưỡng cố định.

Đánh giá theo ba tầng.

### 10.1. Tầng 1 — hành vi tức thời

Phân tích rolling window khoảng 10–20 giây:

- acceleration variance,
- jerk,
- speed variance,
- braking,
- stop/start,
- speed oscillation,
- lean changes.

Ví dụ:

```
45 → 50 → 43 → 52 → 44 km/h
```

trong khoảng thời gian ngắn có thể biểu thị ga không ổn định.

---

### 10.2. Tầng 2 — lịch sử của chính người dùng

App dần học:

> Khi người dùng này chạy trong điều kiện tương tự, cách lái nào thường đi kèm với hiệu suất nhiên liệu tốt hơn?

Ví dụ:

- lịch sử tốt có acceleration variance thấp,
- Current có acceleration variance cao hơn 38%,

thì app có thêm bằng chứng rằng cách chạy hiện tại kém hiệu quả hơn baseline cá nhân.

---

### 10.3. Tầng 3 — Best Session

Nếu route hiện tại có một Best Session phù hợp, app dùng Best Session làm reference/ghost để so realtime.

---

## 11. Realtime Alert

Nguyên tắc:

- Không cảnh báo liên tục.
- Chỉ cảnh báo khi người lái có hành động cụ thể để điều chỉnh.
- Ưu tiên âm thanh/rung hơn việc bắt người dùng nhìn màn hình.
- Có cooldown để tránh lặp.
- Khi trạng thái trở về bình thường, cảnh báo tự biến mất.

Ví dụ:

### Ga dao động

```
GIỮ GA ĐỀU
```

Voice:

> Giữ ga đều.

### Tăng ga mạnh

```
TĂNG GA MẠNH
Nhả nhẹ ga
```

### Đã ổn định trở lại

- rung nhẹ một lần,
- không cần voice liên tục.

Không đọc các thông báo kiểu:

> Điểm hiện tại là 73.

vì không giúp người lái hành động trực tiếp.

---

## 12. Driver-caused vs Environment-caused

App không được đánh đồng đường đông với lái xe kém.

Cần cố gắng phân biệt:

### Driver-caused

- ga dao động,
- hard acceleration,
- hard braking,
- jerk,
- speed hunting.

### Environment-caused

- dừng lâu do traffic,
- stop & go do đường đông,
- tốc độ tuyến giảm,
- thay đổi route,
- tín hiệu GPS kém.

Traffic làm chuyến đi chậm hơn không mặc định làm giảm Driving Efficiency của người dùng.

---

## 13. Session

Một Session đại diện cho một chuyến chạy.

Thông tin tổng:

- session ID,
- start timestamp,
- end timestamp,
- duration,
- distance,
- average speed,
- max speed,
- stopped duration,
- moving duration,
- efficiency score,
- smooth acceleration score,
- smooth braking score,
- hard acceleration count,
- hard braking count,
- max lean left,
- max lean right,
- estimated fuel used,
- estimated range after session,
- route signature/reference ID nếu có.

---

## 14. Best Session / Reference Session

### 14.1. Không đồng nhất "Best" với "Fastest"

Cần phân biệt:

- **Best Efficient Session**
- **Fastest Session**

Mặc định hệ quy chiếu của ứng dụng là:

> **Best Efficient Session**

Không mặc định chọn chuyến nhanh nhất.

---

### 14.2. Reference

App có thể tự chọn Best Efficient Session.

Người dùng cũng có thể pin thủ công một Session làm Reference trong tương lai.

Reference Session đóng vai trò như một **Ghost Driver**.

---

## 15. Ghost Comparison

Khi route hiện tại phù hợp với một Reference Session:

```
BEST       ----- ghost
CURRENT    _____ realtime
```

App hiển thị hai đường biểu đồ gần như chồng lên nhau để người dùng quan sát mức lệch.

---

## 16. So sánh theo cùng vị trí

Không chỉ so:

> giây thứ N của Best

với:

> giây thứ N của Current.

Cần ưu tiên so tại **cùng vị trí/quãng đường trên route**.

Ví dụ tại km 4.72:

Best:

```
11:32
42 km/h
```

Current:

```
12:04
39 km/h
```

App có thể xác định:

```
Current +32s so với Best
```

Cách này giúp tránh sai lệch do hai chuyến không tiến triển cùng tốc độ theo thời gian.

---

## 17. Biểu đồ chính

Biểu đồ ưu tiên:

- X = thời gian.
- Y = quãng đường tích lũy.

Hai series:

- Best,
- Current.

Ý nghĩa:

- đường dốc hơn → quãng đường tăng nhanh hơn,
- đoạn gần ngang → đứng/dừng,
- hai đường gần chồng nhau → tiến trình tương đồng.

Khi cần so theo route, engine vẫn sử dụng vị trí/quãng đường tương ứng để alignment.

---

## 18. Future Projection

Ứng dụng cần dự đoán:

> Nếu người dùng giữ nguyên cách chạy hiện tại, kết quả cuối chuyến sẽ như thế nào?

Prediction có thể sử dụng:

- recent progress rate,
- recent speed pattern,
- recent efficiency,
- deviation so với Best,
- remaining distance,
- historical performance.

---

### 18.1. Dự đoán thời gian

Ví dụ:

```
Best           31:20
Projected      32:48
Difference     +1:28
```

---

### 18.2. Dự đoán efficiency

Ví dụ:

```
Best           94
Projected      87
```

---

### 18.3. Dự đoán nhiên liệu

Ví dụ:

```
Best fuel      0.29 L
Projected      0.31 L
Difference     +6.9%
```

Các giá trị nhiên liệu là estimate, không được thể hiện như số đo trực tiếp từ xe.

---

## 19. Biểu diễn prediction trên chart

Có thể hiển thị ba dạng đường:

```
Best        - - - - - - - - -

Current     ━━━━━━━━━●

Prediction           · · · · · ·
```

- Current chỉ tới thời điểm hiện tại.
- Prediction kéo dài từ current point tới expected end.
- Best hiển thị toàn tuyến.

Mục tiêu:

> Nhìn nhanh là biết nếu tiếp tục cách chạy này thì current sẽ kết thúc ở đâu so với Best.

---

## 20. Fuel Tracking

Người dùng có thể ghi nhận mỗi lần đổ xăng.

Thông tin:

- timestamp,
- liters,
- total price,
- price per liter,
- full tank: yes/no,
- odometer nếu người dùng muốn nhập,
- distance tracked by app kể từ chu kỳ trước.

Field quan trọng:

```
☑ Đổ đầy bình
```

---

## 21. Full-tank Fuel Cycle

Ví dụ:

Full Tank A

Sau đó chạy:

```
182 km
```

Full Tank B:

```
4.2 L
```

Mức tiêu hao thực tế của chu kỳ:

```
182 / 4.2 = 43.3 km/L
```

Nếu giữa hai lần full có các lần đổ lẻ:

```
Full A
+ 1.5 L
+ 1.0 L
+ 2.8 L → Full B
```

thì tổng fuel consumed của cycle được tính từ các lần fill phù hợp trong chu kỳ.

---

## 22. Fuel Economy Model v0.1

Không sử dụng AI/ML ngay.

Ban đầu dùng:

- rolling average,
- weighted rolling average,
- ưu tiên các chu kỳ gần đây.

Ví dụ tư duy weighting:

```
Recent cycle        50%
Previous cycle      30%
Older history       20%
```

Weight cụ thể chưa chốt.

---

## 23. Estimated Remaining Range

Ví dụ:

Observed economy:

```
43 km/L
```

Tank capacity:

```
8 L
```

Full range:

```
~344 km
```

Sau 100 km:

```
Estimated consumed ≈ 100 / 43
                   ≈ 2.33 L
```

Estimated fuel left:

```
8 - 2.33 = 5.67 L
```

Estimated range:

```
5.67 × 43 ≈ 244 km
```

UI không được thể hiện đây là số tuyệt đối chính xác.

Nên hiển thị:

```
Ước tính còn ~240 km
```

Có thể kèm:

- confidence,
- range interval,

khi engine đủ dữ liệu.

---

## 24. Confidence của Fuel Prediction

Khi chưa đủ dữ liệu:

```
Chưa đủ dữ liệu
```

Sau một chu kỳ:

```
43.3 km/L
Độ tin cậy thấp
```

Sau nhiều chu kỳ:

```
42.1 km/L
Độ tin cậy tốt
```

Mức confidence cụ thể sẽ được thiết kế ở vòng thuật toán sau.

---

## 25. UI/UX chính

### 25.1. Mục tiêu

Giảm tối đa số màn hình.

Trong lúc lái, người dùng gần như chỉ cần **một Driving Dashboard**.

Thiết kế theo tinh thần:

- dashboard xe,
- HUD,
- gauge,
- đồng hồ,
- ít text,
- ít metric cùng lúc,
- contextual information.

---

## 26. Driving Dashboard

Mặt đồng hồ chính cần tạo cảm giác như dashboard phương tiện thay vì một danh sách số liệu.

Ví dụ bố cục:

```
                 BEST +12s

             ╭────────────╮
          ╭──╯            ╰──╮
        ╱                      ╲

       │          48            │
       │         km/h           │

       │        ● 92            │
       │      EFFICIENT         │

        ╲                      ╱
          ╰──╮            ╭──╯
             ╰────────────╯

        ━━━━━━━━━━━━━━━━━━━
        CURRENT
        ┄┄┄┄┄┄┄┄┄┄┄ BEST

       7.4 km          ~186 km

            GIỮ GA ĐỀU
```

---

## 27. Multi-layer Gauge

Không tạo hàng loạt đồng hồ nhỏ luôn hiển thị.

Một gauge trung tâm có thể có nhiều lớp.

### Trung tâm

Vận tốc realtime:

```
48 km/h
```

### Vòng ngoài

Speed indication.

### Efficiency ring

Realtime Efficiency.

Ví dụ:

```
92
EFFICIENT
```

Gauge có thể có các trạng thái:

- ECO,
- GOOD,
- AGGRESSIVE.

Cách mapping màu/zone sẽ được quyết định ở phần visual design sau.

---

## 28. Contextual UI

UI tự thay đổi theo tình huống.

### Bình thường

Chỉ cần:

```
48 km/h

Efficiency 92

7.4 km
~186 km range
```

---

### Khi phát hiện ga bất thường

Một thông tin ít quan trọng như range có thể tạm biến mất.

Thay bằng:

```
GA DAO ĐỘNG
Giữ ga đều
```

Sau vài giây trạng thái tự trở lại bình thường.

---

### Hard acceleration

```
TĂNG GA MẠNH
Nhả nhẹ ga
```

---

### Current lệch Best

Hiển thị nhỏ:

```
BEST
+23 sec
```

Không nhất thiết phát voice.

---

### Efficiency tốt hơn reference

Có thể hiển thị tạm:

```
▲ +4% efficiency
```

rồi tự biến mất.

---

## 29. Lean UI

Không hiển thị lean angle liên tục.

Bình thường:

- ẩn.

Khi đang cua:

- lean indicator xuất hiện.

Ví dụ:

```
      \ |
       \|
      18°
```

Ra khỏi cua:

- tự ẩn.

Cuối Session mới hiển thị tổng:

```
Max L 24°
Max R 27°
```

---

## 30. Giảm số màn hình

Không cần nhiều tab luôn hiện trên navigation.

Ưu tiên:

### Màn hình chính

Driving Dashboard.

### Nội dung phụ

Dùng:

- bottom sheet,
- swipe,
- modal,
- contextual card,

khi xe đang dừng.

Các nội dung phụ:

- Session History,
- Fuel History,
- Settings,
- Calibration,
- Compare details.

---

## 31. Forecast UI

Không hiển thị Time + Efficiency + Fuel prediction cùng lúc.

Chỉ show một metric chính, có thể chuyển giữa:

### TIME

```
31:20 best
32:48 projected
```

### EFFICIENCY

```
94 best
87 projected
```

### FUEL

```
0.29 L best
0.31 L projected
```

Mục tiêu là tránh làm người lái quá tải thông tin.

---

## 32. Kiến trúc ứng dụng dự kiến

```
Android APK
    │
    ├── Tracking Foreground Service
    │       ├── GPS / GNSS
    │       ├── Accelerometer
    │       ├── Gyroscope
    │       └── Rotation Vector
    │
    ├── Sensor Fusion
    │
    ├── Session Engine
    │
    ├── Realtime Driving Analyzer
    │
    ├── Alert Engine
    │
    ├── Best Session / Ghost Engine
    │
    ├── Prediction Engine
    │
    ├── Fuel Economy Engine
    │
    └── Local Database
```

---

## 33. Công nghệ dự kiến

Chưa khóa cứng nhưng hiện tại nghiêng về:

- Kotlin.
- Jetpack Compose.
- Room Database.
- Android Foreground Service.
- Android/GNSS location stack hoặc Fused Location Provider tùy quyết định về dependency.

Mục tiêu:

- chạy offline,
- ít dependency,
- dễ debug,
- dễ xuất APK Android 13.

---

## 34. Data model sơ bộ

Các entity dự kiến:

### Session

Thông tin tổng của chuyến.

### TrackPoint

GPS/GNSS timeline.

### MotionSample

Sensor timeline hoặc sample đã được downsample/processed.

### DrivingEvent

Ví dụ:

- hard acceleration,
- hard braking,
- speed oscillation,
- abnormal jerk,
- alert generated.

### FuelEntry

Thông tin một lần đổ xăng.

### FuelCycle

Chu kỳ giữa các lần full tank.

### RouteReference

Route signature và Best Session tương ứng.

---

## 35. Data retention

Chưa chốt.

Cần thảo luận tiếp:

- lưu raw sensor trong bao lâu,
- downsample sau khi kết thúc Session hay không,
- chỉ giữ processed features dài hạn hay giữ raw data,
- giới hạn dung lượng local database.

---

## 36. Các quyết định đã chốt

1. Android 13 APK.
2. Giai đoạn đầu dành cho xe máy.
3. Điện thoại gắn cố định trên xe.
4. Theo dõi liên tục trong Session.
5. Có realtime anomaly detection.
6. Có realtime driving feedback.
7. Best Efficient Session là reference mặc định.
8. Current Session được so với Best theo kiểu ghost.
9. Biểu đồ chính chồng Current và Best.
10. Comparison cần ưu tiên alignment theo cùng vị trí/quãng đường.
11. Có future projection nếu giữ cách chạy hiện tại.
12. Forecast gồm ít nhất time, efficiency và estimated fuel.
13. Theo dõi các lần đổ xăng.
14. Ước tính km/L từ lịch sử full-tank cycle.
15. Dự đoán remaining range.
16. Không tuyên bố remaining fuel/range là số đo chính xác.
17. UI tập trung vào một Driving Dashboard.
18. Dashboard dùng gauge/mặt đồng hồ thay vì chỉ hiển thị số.
19. Thông tin hiển thị theo context.
20. Tránh show quá nhiều metric cùng lúc.
21. Trong lúc lái ưu tiên voice/rung cho cảnh báo quan trọng.
22. Traffic/environment không mặc định bị coi là lỗi của người lái.
23. V0.1 ưu tiên thuật toán có thể giải thích được, chưa cần AI/ML phức tạp.

---

## 37. Các vấn đề cần tiếp tục thảo luận

### Thuật toán Efficiency

- công thức điểm,
- weight từng feature,
- normalization,
- hysteresis,
- rolling window,
- giới hạn ảnh hưởng traffic.

### Realtime alert

- threshold,
- cooldown,
- severity,
- voice phrases,
- vibration pattern,
- khi nào cảnh báo và khi nào im lặng.

### Best Session

- công thức chọn Best Efficient Session,
- minimum number of Sessions,
- route similarity,
- handling traffic/weather/time-of-day.

### Ghost alignment

- theo distance,
- GPS position,
- map matching hay không,
- tolerance khi route hơi khác.

### Prediction

- phương pháp extrapolation,
- confidence,
- cách xử lý traffic thay đổi giữa chặng.

### Fuel model

- partial fills,
- full-tank detection,
- missing trips,
- user không mở app trong một chuyến,
- tank capacity,
- confidence.

### UI

- hình dạng gauge,
- màu sắc,
- animation,
- landscape/portrait,
- always-on behavior,
- day/night mode.

### Battery

- GPS interval,
- sensor sampling,
- screen-off mode,
- adaptive sampling.

### Session start/stop

- manual Start/Stop trước,
- auto-detection để sau.

---

## 38. Mục tiêu của vòng thảo luận tiếp theo

Ưu tiên tiếp theo:

> **Thiết kế thuật toán "Lái xe hiệu quả" và hệ thống realtime alert.**

Cần biến khái niệm "lái hiệu quả" thành các metric có thể đo, kiểm chứng và giải thích được trước khi bắt đầu code production.
