package com.example.pdftools.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    // Vibrant but soft gradient brushes for each page
    val gradients = listOf(
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F2027),
                Color(0xFF203A43),
                Color(0xFF2C5364)
            )
        ),
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1F1C2C),
                Color(0xFF928DAB)
            )
        ),
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2C3E50),
                Color(0xFF000000)
            )
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradients[pagerState.currentPage])
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(
                page = page,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        }

        // Top Skip Button
        if (pagerState.currentPage < 2) {
            TextButton(
                onClick = onFinished,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.8f))
            ) {
                Text(text = "Skip", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Bottom Bar (Indicators & Navigation)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width = animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = tween(durationMillis = 300),
                        label = "IndicatorWidth"
                    )
                    val alpha = animateFloatAsState(
                        targetValue = if (isSelected) 1.0f else 0.4f,
                        animationSpec = tween(durationMillis = 300),
                        label = "IndicatorAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(height = 8.dp, width = width.value)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = alpha.value))
                    )
                }
            }

            // Navigation Button (Next or Get Started)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (pagerState.currentPage == 2) {
                    Button(
                        onClick = onFinished,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Get Started",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: Int,
    modifier: Modifier = Modifier
) {
    val title = when (page) {
        0 -> "All Your PDF Tools in One Place"
        1 -> "Process Locally, Stay Private"
        else -> "Customize Your Workflow"
    }

    val description = when (page) {
        0 -> "Merge, split, organize, crop, and convert PDFs directly on your device. Everything you need, unified in one single application."
        1 -> "No uploads. No cloud servers. All document processing happens offline on your device, ensuring maximum security and privacy for your files."
        else -> "Select your preferred image compression quality, DPI settings, system theme, and file saving destinations to tailor the experience to your needs."
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1.2f),
            contentAlignment = Alignment.Center
        ) {
            when (page) {
                0 -> ToolsFanIllustration(modifier = Modifier.fillMaxSize())
                1 -> PrivacyShieldIllustration(modifier = Modifier.fillMaxSize())
                else -> CustomizeDashboardIllustration(modifier = Modifier.fillMaxSize())
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(64.dp)) // Extra space to prevent overlapping bottom content
    }
}

@Composable
fun ToolsFanIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Central Document Card
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Article,
                contentDescription = null,
                tint = Color(0xFF2C3E50),
                modifier = Modifier.fillMaxSize()
            )
        }

        // Fanning Category Icons
        val items = listOf(
            Triple(Icons.Filled.CallSplit, Color(0xFFFF6B6B), 0f),      // Right
            Triple(Icons.Filled.Compress, Color(0xFF4CD97B), 72f),     // Bottom Right
            Triple(Icons.Filled.ArrowUpward, Color(0xFFFFBB33), 144f), // Bottom Left
            Triple(Icons.Filled.Edit, Color(0xFFBB6BD9), 216f),        // Top Left
            Triple(Icons.Filled.Lock, Color(0xFF1ABC9C), 288f)         // Top Right
        )

        items.forEachIndexed { index, item ->
            val angleRad = Math.toRadians(item.third.toDouble())
            // Radius of the circle of icons
            val radius = 80.0

            val posX = (radius * Math.cos(angleRad)).dp
            val posY = (radius * Math.sin(angleRad)).dp

            Box(
                modifier = Modifier
                    .offset(x = posX, y = posY)
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(item.second)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.first,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun PrivacyShieldIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glowing Outer Ring (Static)
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(Color(0xFF1ABC9C).copy(alpha = 0.2f))
        )

        // Mid Ring (Static glow)
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFF1ABC9C).copy(alpha = 0.15f))
        )

        // Central Shield / Lock Card
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1ABC9C), Color(0xFF16A085))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        // Mini Lock overlapping on shield
        Box(
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.Center)
                .offset(x = 22.dp, y = 22.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Color(0xFF16A085),
                modifier = Modifier.fillMaxSize()
            )
        }

        // Orbiting particles (little locks / keys - static position)
        val orbitRadius = 75.0
        val numParticles = 3
        for (i in 0 until numParticles) {
            val angleRad = Math.toRadians((i * (360 / numParticles)).toDouble())
            val posX = (orbitRadius * Math.cos(angleRad)).dp
            val posY = (orbitRadius * Math.sin(angleRad)).dp
            Box(
                modifier = Modifier
                    .offset(x = posX, y = posY)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = Color(0xFF16A085),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun CustomizeDashboardIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Card 1: Back Card (Compression Quality)
        Card(
            modifier = Modifier
                .offset(x = (-30).dp, y = (-15).dp)
                .graphicsLayer {
                    rotationZ = -8f
                }
                .width(130.dp)
                .height(85.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF34495E)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quality",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "85%",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Filled.Compress,
                        contentDescription = null,
                        tint = Color(0xFFFFBB33),
                        modifier = Modifier.size(18.dp)
                    )
                }
                // Custom mini progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight()
                            .background(Color(0xFFFFBB33))
                    )
                }
            }
        }

        // Card 2: Mid Card (DPI Settings)
        Card(
            modifier = Modifier
                .offset(x = 35.dp, y = (-10).dp)
                .graphicsLayer {
                    rotationZ = 6f
                }
                .width(120.dp)
                .height(75.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E3A5F)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Resolution",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "300 DPI",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Filled.GridOn,
                        contentDescription = null,
                        tint = Color(0xFF4CD97B),
                        modifier = Modifier.size(16.dp)
                    )
                }
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF4CD97B).copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "HD Print Quality",
                        color = Color(0xFF4CD97B),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Card 3: Front Card (Theme Settings / Settings Gear)
        Card(
            modifier = Modifier
                .offset(x = 0.dp, y = 25.dp)
                .width(140.dp)
                .height(90.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C3E50)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dark Mode",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                    // Custom switch indicator
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBB6BD9))
                            .padding(2.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }

                Text(
                    text = "Personalize colors",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 8.sp
                )
            }
        }
    }
}

