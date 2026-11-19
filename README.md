# GiroCerto

Aplicativo Android nativo desenvolvido em **Kotlin**, **Jetpack Compose** e **Material 3** voltado à gestão operacional, financeira e colaborativa de entregadores de aplicativo e motoboys de São Paulo.

## Objetivos do Projeto
- Substituir planilhas genéricas por um painel adaptado às condições reais da pilotagem (sol forte, chuva, luvas e paradas rápidas no semáforo).
- Garantir alta legibilidade (modos Diurno, Noturno e HUD preto OLED de altíssimo contraste), alvos de toque amplos (≥ 64 dp) e prevenção de erros em lançamentos financeiros.
- Consolidar ganhos multiplataforma, controle de lucro líquido, rede de alertas urbanos (Fórum) e modo somente leitura durante a pilotagem.

## Arquitetura e Tecnologias
- **Linguagem & UI:** Kotlin, Jetpack Compose, Material 3, Single-Activity, Navigation Compose.
- **Arquitetura:** MVVM com separação em camadas (`core`, `domain`, `data`, `feature`) e injeção via `AppContainer` + `viewModelFactory`.
- **Persistência Offline-First:** Room (SQLite) com `Flow` reativo, valores monetários em centavos inteiros (`Long`), UUIDs locais, flag `syncPending` para futura sincronização com API e `MIGRATION_1_2` preservando dados existentes.
- **Sensores e Recursos Nativos:** `LocationManagerCompat` (GPS sem dependência de Play Services) com motor de histerese para ativação do HUD e `TextToSpeech` (Copiloto por voz).
- **Testes:** JUnit 5, Turbine, `kotlinx-coroutines-test` e Compose UI Test.

## Funcionalidades Implementadas
1. **Ganhos Multiplataforma e Registro Rápido:** Lançamento em poucos toques (valor em R$ → plataforma Uber, 99, iFood, Rappi, Keeta, Zé Delivery, Outro ou categoria de gasto), bloqueio de valores inválidos, tela de confirmação anti-erro (`Confirmar` / `Corrigir`) e `Desfazer` por 5 segundos.
2. **Visão Geral e Histórico:** Cálculo automático do lucro líquido do dia (`ganhos - gastos`), ganho por hora vs. meta diária editável e histórico diário/semanal.
3. **Fórum de Apoio SP:** Alertas de Blitz, Acidentes, Pontos de Apoio e Mecânica com busca, publicação local, sistema de apoio e moderação (denúncia e bloqueio).
4. **Perfil e Configurações:** Edição de apelido, modelo da moto, meta diária e alternância persistida entre Modo Diurno e Modo Noturno.
5. **Modo Pilotagem (HUD):** Interface somente leitura em preto OLED com cronômetro de tempo logado em tempo real, ganho/hora, progresso da meta, clima/vento, último alerta das ruas e integração de velocidade via GPS/Copiloto por voz.
6. **Semáforo Pré-Corrida (`R$/km`):** Calculadora rápida que avalia ofertas como Verde, Amarelo ou Vermelho descontando o custo/km da moto *(se aplicada a Etapa 4B)*.

## Como Executar
1. Abra a pasta raiz `GiroCerto` no Android Studio (JDK 17+ / Kotlin DSL).
2. Execute **Sync Project with Gradle Files**.
3. Rode o módulo `app` em um emulador ou dispositivo Android (minSdk 26 / Android 8.0+).
4. Para rodar os testes unitários: clique direito em `br.com.girocerto (test)` → **Run**.