package com.moneymentor.MoneyMentor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MoneyMentorApplication

fun main(args: Array<String>) {
	runApplication<MoneyMentorApplication>(*args)
}
