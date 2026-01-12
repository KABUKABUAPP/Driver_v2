//package com.kabukabu.driver.features.analytics.presentation
//
//import androidx.compose.foundation.*
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.KeyboardArrowDown
//import androidx.compose.material.icons.filled.MoreVert
//import androidx.compose.material.icons.outlined.Menu
//import androidx.compose.material.icons.outlined.Place
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.kabukabu.driver.core.theme.KabukabuYellow
//import com.kabukabu.driver.features.analytics.data.AnalyticsData
//import com.kabukabu.driver.features.analytics.data.Earnings2
//import com.kabukabu.driver.features.analytics.data.PaymentDetail
//import com.kabukabu.driver.features.analytics.data.PaymentMethods2
//import com.kabukabu.driver.features.analytics.data.TripDay
//import com.kabukabu.driver.features.analytics.data.TripStats
//
//// Color Palette
//val BgGray = Color(0xFFF8F8F8)
//val ChartYellow = KabukabuYellow
//val ChartBlue = Color(0xFF3341ED)
//val TextGray = Color(0xFF8E8E8E)
//
//@Composable
//fun NewAnalyticsScreen(data: AnalyticsData) {
//    Scaffold(
//        topBar = { AnalyticsHeader() },
//        containerColor = BgGray
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .padding(paddingValues)
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            EarningsCard(data.your_earnings)
//
//            // 2x2 Grid for Stats
//            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                StatCard("Wallet Balance", "₦${data.wallet_balance.toInt()}", "Go to wallet", Modifier.weight(1f))
//                StatCard("Due Payment", "₦${data.due_payments.toInt()}", "Go to due payment", Modifier.weight(1f))
//            }
//            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                StatCard("Average ratings", "${data.average_rating}", null, Modifier.weight(1f))
//                StatCard("Driver Score", "${data.average_rating}", null, Modifier.weight(1f)) // Using rating as placeholder for score
//            }
//
//            TripsBarChartCard(data.total_trips_data, data.trips_chart)
//
//            PaymentMethodsCard(data.payment_methods)
//
//            Spacer(modifier = Modifier.height(20.dp))
//        }
//    }
//}
//
//@Composable
//fun AnalyticsHeader() {
//    Column(
//        modifier = Modifier.fillMaxWidth().background(Color.White).padding(top = 16.dp, bottom = 8.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
//            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.align(Alignment.CenterStart))
//            Text("Analytics", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
//        }
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Text("This week", color = TextGray, fontSize = 14.sp)
//            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
//        }
//    }
//}
//
//@Composable
//fun EarningsCard(earnings: Earnings2) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(24.dp).fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("You earned", color = TextGray, fontSize = 14.sp)
//            Text("₦${earnings.current.toInt()}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
//            Text("This week", color = TextGray, fontSize = 14.sp)
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Green, modifier = Modifier.size(16.dp))
//                Text(earnings.comment, color = TextGray, fontSize = 12.sp)
//            }
//        }
//    }
//}
//
//@Composable
//fun StatCard(title: String, value: String, linkText: String?, modifier: Modifier) {
//    Card(
//        modifier = modifier.height(110.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize().padding(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(title, color = TextGray, fontSize = 12.sp)
//            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
//            linkText?.let {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(it, color = TextGray, fontSize = 11.sp)
//                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextGray, modifier = Modifier.size(12.dp))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun TripsBarChartCard(stats: TripStats, chartData: List<TripDay>) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                Text("Total Trips completed", fontWeight = FontWeight.Bold)
//                Text("${stats.total_trips}", fontWeight = FontWeight.Bold)
//            }
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Bar Chart
//            Row(
//                modifier = Modifier.fillMaxWidth().height(120.dp),
//                horizontalArrangement = Arrangement.SpaceEvenly,
//               verticalAlignment = Alignment.Bottom
//            ) {
//                val maxTrips = chartData.maxOfOrNull { it.no_of_trips }?.coerceAtLeast(1) ?: 1
//                chartData.forEach { day ->
//                    val barHeightFraction = day.no_of_trips.toFloat() / maxTrips
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Box(
//                            modifier = Modifier
//                                .width(24.dp)
//                                .fillMaxHeight(barHeightFraction.coerceAtLeast(0.1f))
//                                .background(ChartYellow, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
//                        )
//                    }
//                }
//            }
//            // Labels Row
//            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
//                listOf("Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat").forEach { dayString ->
//                    Text(dayString, fontSize = 10.sp, color = TextGray)
//                }
//            }
//
//            Divider(modifier = Modifier.padding(vertical = 16.dp), color = BgGray)
//
//            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Outlined.Place, contentDescription = null, modifier = Modifier.size(16.dp))
//                    Spacer(Modifier.width(4.dp))
//                    Text("${stats.total_distance_in_km}km", fontWeight = FontWeight.Bold, fontSize = 14.sp)
//                }
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Outlined.Menu, contentDescription = null, modifier = Modifier.size(16.dp))
//                    Spacer(Modifier.width(4.dp))
//                    val hours = stats.total_time_in_minutes / 60
//                    val mins = stats.total_time_in_minutes % 60
//                    Text("${hours}hr, ${mins}min", fontWeight = FontWeight.Bold, fontSize = 14.sp)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun PaymentMethodsCard(payments: PaymentMethods2) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
//            Text("Payment methods", fontWeight = FontWeight.Bold)
//            Spacer(modifier = Modifier.height(24.dp))
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                // Donut Chart
//                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
//                    Canvas(modifier = Modifier.size(100.dp)) {
//                        val stroke = 30f
//                        val cashAngle = (payments.cash_payment.percentage.toFloat() / 100) * 360f
//                        val otherAngle = (payments.other_payment.percentage.toFloat() / 100) * 360f
//
//                        drawArc(ChartYellow, -90f, cashAngle, false, style = Stroke(stroke, cap = StrokeCap.Round))
//                        drawArc(ChartBlue, -90f + cashAngle, otherAngle, false, style = Stroke(stroke, cap = StrokeCap.Round))
//                    }
//                }
//                Spacer(modifier = Modifier.width(24.dp))
//                // Legend
//                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                    LegendItem(ChartYellow, "Cash payment (${payments.cash_payment.value})")
//                    LegendItem(ChartBlue, "Card / Wallet payment (${payments.other_payment.value})")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun LegendItem(color: Color, text: String) {
//    Row(verticalAlignment = Alignment.CenterVertically) {
//        Box(modifier = Modifier.size(12.dp).background(color).clip(RoundedCornerShape(2.dp)))
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun NewAnalyticsScreenPreview() {
//    val sampleData = AnalyticsData(
//        your_earnings = Earnings2(
//            current = 0.0,
//            previous = 7144.0,
//            comment = "You Earned Infinity% Less Than Previous Timeline"
//        ),
//        wallet_balance = 17440.0,
//        due_payments = 420.0,
//        average_rating = 0.0,
////        driver_score = 0.0, // Added driver_score to match data class
//        total_trips_data = TripStats(
//            total_distance_in_km = 0.0,
//            total_time_in_minutes = 0,
//            total_trips = 0
//        ),
//        trips_chart = listOf(
//            TripDay(date = "Sun Dec 28 2025", no_of_trips = 0),
//            TripDay(date = "Mon Dec 29 2025", no_of_trips = 0),
//            TripDay(date = "Tue Dec 30 2025", no_of_trips = 0),
//            TripDay(date = "Wed Dec 31 2025", no_of_trips = 0),
//            TripDay(date = "Thu Jan 01 2026", no_of_trips = 0),
//            TripDay(date = "Fri Jan 02 2026", no_of_trips = 0),
//            TripDay(date = "Sat Jan 03 2026", no_of_trips = 0)
//        ),
//        payment_methods = PaymentMethods2(
//            total = 0,
//            cash_payment = PaymentDetail(value = 0, percentage = 20.0),
//            other_payment = PaymentDetail(value = 0, percentage = 80.0)
//        )
//    )
//    NewAnalyticsScreen(data = sampleData)
//}
