package com.six_m.uniform;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UniformApplication {

	/* As datas preenchidas pelo servidor (entrega sem data, saída do aluno, requisição)
	   usam o relógio da JVM. Em container ele é UTC, o que adiantava tudo em 3 h e jogava
	   registros das 21h em diante para o dia (ou mês) seguinte nos relatórios. */
	static final String FUSO_DA_ESCOLA = "America/Sao_Paulo";

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(System.getenv().getOrDefault("APP_TIMEZONE", FUSO_DA_ESCOLA)));
		SpringApplication.run(UniformApplication.class, args);
	}

}
