package com.kushwahahardware.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.compose.material3.MaterialTheme

@Composable
fun SalesChart(
    salesData: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                setDrawGridBackground(false)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = onSurfaceColor
                    granularity = 1f
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    textColor = onSurfaceColor
                    gridColor = onSurfaceColor and 0x22FFFFFF
                }
                
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = salesData.mapIndexed { index, pair ->
                Entry(index.toFloat(), pair.second)
            }
            
            val dataSet = LineDataSet(entries, "Sales").apply {
                color = primaryColor
                valueTextColor = onSurfaceColor
                lineWidth = 2f
                setDrawCircles(true)
                setCircleColor(primaryColor)
                circleRadius = 4f
                setDrawFilled(true)
                fillColor = primaryColor
                fillAlpha = 50
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(salesData.map { it.first })
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}
