package io.github.onlyashd.hukiawards.service

import io.github.onlyashd.hukiawards.model.Category
import io.github.onlyashd.hukiawards.model.LeaderboardEntry
import io.github.onlyashd.hukiawards.model.UserProfile
import io.github.onlyashd.hukiawards.model.VoteRequest
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

class ImageService {
    fun generateLeaderboard(
        eventName: String,
        categoryName: String,
        entries: List<LeaderboardEntry>
    ): BufferedImage {
        val width = 800
        val height = 1000
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()

        setupGraphics(graphics)

        // Background
        graphics.color = Color(18, 18, 20)
        graphics.fillRect(0, 0, width, height)

        // Title Header
        graphics.color = Color(150, 150, 255)
        graphics.font = Font("SansSerif", Font.BOLD, 22)
        graphics.drawString("LIDERANÇA PARCIAL - ${eventName.uppercase()}", 40, 50)

        graphics.color = Color.WHITE
        graphics.font = Font("SansSerif", Font.BOLD, 36)
        graphics.drawString(categoryName.uppercase(), 40, 95)

        // Top 10 Entries
        var currentY = 150
        entries.take(10).forEachIndexed { index, entry ->
            drawLeaderboardRow(graphics, index + 1, entry, currentY, width)
            currentY += 80
        }

        drawFooter(graphics, eventName, width, height, 60)
        graphics.dispose()
        return image
    }

    fun generateWinnerCard(
        eventName: String,
        categoryName: String,
        winner: LeaderboardEntry
    ): BufferedImage {
        val width = 800
        val height = 900
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()

        setupGraphics(graphics)

        // Background
        graphics.color = Color(18, 18, 20)
        graphics.fillRect(0, 0, width, height)

        // Golden Frame
        graphics.color = Color(255, 215, 0, 40)
        graphics.fill(
            RoundRectangle2D.Double(
                30.0,
                30.0,
                (width - 60).toDouble(),
                (height - 60).toDouble(),
                20.0,
                20.0
            )
        )
        graphics.color = Color(18, 18, 20)
        graphics.fill(
            RoundRectangle2D.Double(
                40.0,
                40.0,
                (width - 80).toDouble(),
                (height - 80).toDouble(),
                15.0,
                15.0
            )
        )

        // Title Header
        graphics.color = Color(255, 215, 0)
        graphics.font = Font("SansSerif", Font.BOLD, 24)
        val headerText = "VENCEDOR - ${eventName.uppercase()}"
        val headerMetrics = graphics.fontMetrics
        graphics.drawString(headerText, (width - headerMetrics.stringWidth(headerText)) / 2, 100)

        // Category Name
        graphics.color = Color.WHITE
        graphics.font = Font("SansSerif", Font.BOLD, 48)
        val catName = categoryName.uppercase()
        val catMetrics = graphics.fontMetrics
        graphics.drawString(catName, (width - catMetrics.stringWidth(catName)) / 2, 170)

        // Game Cover
        val coverWidth = 400
        val coverHeight = 533 // 3:4 aspect ratio approx
        val coverX = (width - coverWidth) / 2
        val coverY = 220

        try {
            // Try to get a high quality cover
            val highQualUrl = winner.boxArtUrl
                .replace("t_thumb", "t_720p")
                .replace("t_cover_small", "t_720p")

            val cover = ImageIO.read(URI.create(highQualUrl).toURL())

            // Draw shadow/glow
            graphics.color = Color(255, 215, 0, 30)
            graphics.fill(
                RoundRectangle2D.Double(
                    (coverX - 10).toDouble(),
                    (coverY - 10).toDouble(),
                    (coverWidth + 20).toDouble(),
                    (coverHeight + 20).toDouble(),
                    10.0,
                    10.0
                )
            )

            graphics.drawImage(cover, coverX, coverY, coverWidth, coverHeight, null)

            // Cover Border
            graphics.color = Color(255, 215, 0)
            graphics.stroke = BasicStroke(3f)
            graphics.drawRect(coverX, coverY, coverWidth, coverHeight)
        } catch (e: Exception) {
            graphics.color = Color(30, 30, 35)
            graphics.fillRect(coverX, coverY, coverWidth, coverHeight)
        }

        // Game Title
        graphics.color = Color.WHITE
        graphics.font = Font("SansSerif", Font.BOLD, 36)
        val title = winner.title
        val titleMetrics = graphics.fontMetrics

        // Handle long titles
        if (titleMetrics.stringWidth(title) > width - 100) {
            graphics.font = Font("SansSerif", Font.BOLD, 28)
            val smallMetrics = graphics.fontMetrics
            graphics.drawString(title, (width - smallMetrics.stringWidth(title)) / 2, 820)
        } else {
            graphics.drawString(title, (width - titleMetrics.stringWidth(title)) / 2, 820)
        }

        // Vote count small badge
        graphics.color = Color(255, 215, 0, 100)
        graphics.font = Font("SansSerif", Font.PLAIN, 18)
        val votesText = "${winner.voteCount} votos"
        val vMetrics = graphics.fontMetrics
        graphics.drawString(votesText, (width - vMetrics.stringWidth(votesText)) / 2, 860)

        graphics.dispose()
        return image
    }

    private fun drawLeaderboardRow(
        g: Graphics2D,
        rank: Int,
        entry: LeaderboardEntry,
        y: Int,
        width: Int
    ) {
        // Rank Background
        val isTop3 = rank <= 3
        g.color = when (rank) {
            1 -> Color(255, 215, 0, 40) // Gold
            2 -> Color(192, 192, 192, 40) // Silver
            3 -> Color(205, 127, 50, 40) // Bronze
            else -> Color(40, 40, 45)
        }
        g.fill(
            RoundRectangle2D.Double(
                40.0,
                y.toDouble(),
                (width - 80).toDouble(),
                70.0,
                10.0,
                10.0
            )
        )

        // Rank Number
        g.color = if (isTop3) Color.WHITE else Color(180, 180, 180)
        g.font = Font("SansSerif", Font.BOLD, 24)
        g.drawString("#$rank", 60, y + 43)

        // Game Name
        g.color = Color.WHITE
        g.font = Font("SansSerif", Font.BOLD, 20)
        val title = if (entry.title.length > 40) entry.title.take(37) + "..." else entry.title
        g.drawString(title, 120, y + 43)

        // Vote Count
        g.color = Color(150, 150, 255)
        g.font = Font("SansSerif", Font.PLAIN, 18)
        val voteLabel = if (entry.voteCount == 1) "voto" else "votos"
        val countText = "${entry.voteCount} $voteLabel"
        val metrics = g.fontMetrics
        g.drawString(countText, width - 60 - metrics.stringWidth(countText), y + 43)
    }

    private fun setupGraphics(g: Graphics2D) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )
        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        )
    }

    fun generateVotingSummary(
        eventName: String,
        profile: UserProfile,
        categories: List<Category>,
        votes: List<VoteRequest>
    ): BufferedImage {
        val width = 800
        val headerHeight = 150
        val rowHeight = 100
        val footerHeight = 80
        val height = headerHeight + (categories.size * rowHeight) + footerHeight

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        setupGraphics(g)

        // Background
        g.color = Color(18, 18, 20)
        g.fillRect(0, 0, width, height)

        // Header
        drawHeader(g, eventName, profile)

        // Votes
        var currentY = headerHeight
        for (category in categories) {
            val vote = votes.find { it.categoryId == category.id }
            drawVoteRow(g, category, vote, currentY, width, rowHeight)
            currentY += rowHeight
        }

        // Footer
        drawFooter(g, eventName, width, height, footerHeight)

        g.dispose()
        return image
    }

    private fun drawHeader(g: Graphics2D, eventName: String, profile: UserProfile) {
        // Draw Avatar
        profile.avatarUrl?.let { url ->
            try {
                val avatar = ImageIO.read(URI.create(url).toURL())
                val size = 80
                val x = 40
                val y = 35

                val oldClip = g.clip
                g.clip =
                    Ellipse2D.Double(x.toDouble(), y.toDouble(), size.toDouble(), size.toDouble())
                g.drawImage(avatar, x, y, size, size, null)
                g.clip = oldClip

                // Avatar border
                g.color = Color(150, 150, 255)
                g.stroke = BasicStroke(2f)
                g.drawOval(x, y, size, size)
            } catch (e: Exception) {
                g.color = Color.GRAY
                g.fillOval(40, 35, 80, 80)
            }
        }

        // Draw Name
        g.color = Color.WHITE
        g.font = Font("SansSerif", Font.BOLD, 28)
        g.drawString(profile.name, 140, 75)

        g.font = Font("SansSerif", Font.PLAIN, 18)
        g.color = Color(180, 180, 180)
        g.drawString("Minhas indicações - $eventName", 140, 105)

        // Separator
        g.color = Color(40, 40, 45)
        g.drawLine(40, 140, 760, 140)
    }

    private fun drawVoteRow(
        g: Graphics2D,
        category: Category,
        vote: VoteRequest?,
        y: Int,
        width: Int,
        rowHeight: Int
    ) {
        // Category Name
        g.color = Color(150, 150, 255)
        g.font = Font("SansSerif", Font.BOLD, 16)
        g.drawString(category.name.uppercase(), 120, y + 35)

        // Game Name
        g.color = Color.WHITE
        g.font = Font("SansSerif", Font.BOLD, 22)
        val gameName = vote?.gameName ?: "Não indicado"
        g.drawString(gameName, 120, y + 65)

        // Cover
        vote?.gameCoverUrl?.let { url ->
            try {
                // Ensure we use a higher quality URL if possible (already handled by service usually)
                val cover = ImageIO.read(URI.create(url).toURL())
                val cWidth = 60
                val cHeight = 80

                // Draw cover with rounded corners (simplified here as rect)
                g.drawImage(cover, 40, y + 10, cWidth, cHeight, null)
            } catch (e: Exception) {
                g.color = Color(30, 30, 35)
                g.fillRect(40, y + 10, 60, 80)
            }
        } ?: run {
            g.color = Color(30, 30, 35)
            g.fillRect(40, y + 10, 60, 80)
        }

        // Row Separator
        g.color = Color(30, 30, 35)
        g.drawLine(40, y + rowHeight - 1, 760, y + rowHeight - 1)
    }

    private fun drawFooter(
        g: Graphics2D,
        eventName: String,
        width: Int,
        height: Int,
        footerHeight: Int
    ) {
        val y = height - footerHeight

        g.color = Color(150, 150, 150)
        g.font = Font("SansSerif", Font.ITALIC, 14)
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        g.drawString("Gerado em $date", 40, y + 40)

        g.font = Font("SansSerif", Font.BOLD, 16)
        g.color = Color(150, 150, 255)
        val brand = eventName
        val metrics = g.fontMetrics
        g.drawString(brand, width - metrics.stringWidth(brand) - 40, y + 40)
    }
}
