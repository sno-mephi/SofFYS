package ru.idfedorov09.telegram.bot.util

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object Board {
    fun changeBoard(teamId: Long, problemId: Long, color: String) {
        val column = problemId % 11
        val row = problemId / 11
        val c = 391L
        val x = 115L
        val y = 72L
        val s = 24L

        val image1: BufferedImage = if (color == "POOL") {
            ImageIO.read(File("images/templates/pool.png"))
        } else if (color == "COMPLETED") {
            ImageIO.read(File("images/templates/pool.png"))
        } else {
            return
        }

        val image2 = ImageIO.read(File("images/boards/$teamId.png"))

        val croppedFragment = image1.getSubimage(
            (c + column * (x + s)).toInt(),
            (s + row * (y + s)).toInt(),
            x.toInt(),
            y.toInt(),
        )

        val g2d = image2.createGraphics()
        g2d.drawImage(
            croppedFragment, (c + column * (x + s)).toInt(), (s + row * (y + s)).toInt(),
            (c + column * (x + s) + x).toInt(), (s + row * (y + s) + y).toInt(),
            0, 0, x.toInt(), y.toInt(), null,
        )
        g2d.dispose()

        ImageIO.write(image2, "png", File("images/boards/$teamId.png"))
    }
}
