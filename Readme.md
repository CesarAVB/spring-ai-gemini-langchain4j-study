# Spring AI Gemini LangChain4j Study

Projeto de estudo e exploração da integração de **Inteligência Artificial (IA)** em aplicações Spring Boot, utilizando o modelo de linguagem grande (LLM) **Gemini** do Google AI através da biblioteca **LangChain4j**.

## 📚 Objetivo do Projeto

Demonstrar padrões de design, arquitetura de microserviços e melhores práticas para o desenvolvimento de aplicações inteligentes que integram LLMs de forma escalável e independente.

O projeto foca na criação de um **microserviço de assistentes nutricionais** que opera de forma totalmente autônoma, recebendo dados via DTOs e realizando cálculos de planos nutricionais personalizados.

## 🎯 Funcionalidades Principais

### 1. **Cálculo de Planos Nutricionais**
- Cálculo automático de TMB (Taxa Metabólica Basal) usando fórmula de Harris-Benedict
- Cálculo de gasto calórico diário baseado em fator de atividade
- Distribuição personalizada de macronutrientes (proteína, carboidratos, gordura)
- Geração de recomendações personalizadas por objetivo
- Suporte para múltiplos objetivos: emagrecimento, manutenção, ganho de massa

### 2. **Arquitetura de Assistentes Genéricos**
- Registry centralizado para descoberta automática de assistentes
- Interface genérica `GenericAssistant` para criar novos assistentes
- Integração com LangChain4j para uso de tools (ferramentas de IA)
- System messages customizáveis por assistente

### 3. **API REST Bem Documentada**
- Documentação automática com Swagger/OpenAPI 3.0
- Endpoints para cálculo, validação e consulta de planos
- Exemplos de requisição e resposta disponíveis
- Health checks e informações do serviço

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                    Spring Boot 3.2.5                     │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │           Controllers (HTTP Endpoints)            │   │
│  │  - AssistenteController (POST /plano/calcular)   │   │
│  │  - GenericAssistantController (Assistentes)      │   │
│  └──────────────────────────────────────────────────┘   │
│                          ↓                               │
│  ┌──────────────────────────────────────────────────┐   │
│  │              Services (Business Logic)            │   │
│  │  - PlanoNutricionalCalculatorService (Cálculos)  │   │
│  │  - AssistantPlanoService (Orquestração)          │   │
│  └──────────────────────────────────────────────────┘   │
│                          ↓                               │
│  ┌──────────────────────────────────────────────────┐   │
│  │         Core Framework (Assistentes)              │   │
│  │  - AssistantRegistry (Descoberta)                │   │
│  │  - GenericAssistant (Interface Base)             │   │
│  │  - AssistantTool (Tools para IA)                 │   │
│  └──────────────────────────────────────────────────┘   │
│                          ↓                               │
│  ┌──────────────────────────────────────────────────┐   │
│  │        LangChain4j + Google Gemini                │   │
│  │  (Modelos de IA e processamento de linguagem)     │   │
│  └──────────────────────────────────────────────────┘   │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

### Estrutura de Pacotes

```
br.com.sistema.springaigemini/
├── configurations/
│   ├── AssistantConfig.java        (Beans do projeto)
│   └── OpenApiConfig.java          (Swagger)
├── controllers/
│   ├── AssistenteController.java   (Planos nutricionais)
│   └── GenericAssistantController.java (Assistentes genéricos)
├── core/
│   ├── GenericAssistant.java       (Interface base)
│   ├── AssistantRegistry.java      (Descoberta)
│   ├── AssistantInitializer.java   (Inicialização)
│   ├── BaseAssistantService.java   (Serviço abstrato)
│   └── AssistantTool.java          (Tools para IA)
├── services/
│   ├── PlanoNutricionalCalculatorService.java (Cálculos)
│   └── AssistantPlanoService.java  (Orquestração)
├── tools/
│   └── PlanoAssistantTools.java    (Tools do assistente)
├── models/
│   └── PlanoNutricional.java       (Modelo de domínio)
├── dtos/
│   ├── PacienteDTO.java
│   ├── AvaliacaoFisicaDTO.java
│   ├── PlanoNutricionalDTO.java
│   ├── MacronutrientesDTO.java
│   └── CalculoPlanoCompleteRequest.java
├── mappers/
│   ├── PlanoNutricionalMapper.java (MapStruct)
│   ├── MacronutrientesMapper.java
│   └── CalculoPlanoMapper.java
├── enums/
│   ├── IntensidadeExercicio.java
│   └── ObjetivoNutricional.java
└── Startup.java
```

## 🚀 Como Usar

### Pré-requisitos

- Java 21+
- Maven 3.8+
- Chave de API do Google Gemini
- IDE: IntelliJ IDEA, VS Code ou similar

### Instalação

1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/spring-ai-gemini-langchain4j-study.git
cd spring-ai-gemini-langchain4j-study
```

2. Configure a variável de ambiente com sua chave Gemini:
```bash
export GEMINI_API_KEY="sua-chave-aqui"
export GEMINI_MODEL="gemini-1.5-flash"  # ou outro modelo disponível
```

3. Build do projeto:
```bash
mvn clean install
```

4. Execute a aplicação:
```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### Acessando a Documentação

Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON: `http://localhost:8080/api-docs`

## 📝 Exemplos de Uso

### 1. Calcular Plano Nutricional

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/plano/calcular \
  -H "Content-Type: application/json" \
  -d '{
    "paciente": {
      "id": 1,
      "nome": "João Silva",
      "sexo": "M",
      "altura": 1.75,
      "data_nascimento": "1990-01-15",
      "cpf": "123.456.789-00",
      "email": "joao@email.com",
      "telefone": "11999999999"
    },
    "avaliacaoFisica": {
      "id": 1,
      "peso_atual": 85.5,
      "percentual_gordura": 18.5,
      "massa_magra": 65.6,
      "massa_gorda": 14.9,
      "imc": 26.3,
      "data_avaliacao": "2025-01-20"
    },
    "objetivo": "emagrecimento",
    "intensidadeExercicio": "moderado",
    "observacoes": "opcional"
  }'
```

**Response:**
```json
{
  "pacienteId": 1,
  "nomePaciente": "João Silva",
  "altura_metros": 1.75,
  "peso_atual": 85.5,
  "idade": 35,
  "objetivo": "emagrecimento",
  "intensidade_exercicio": "moderado",
  "tmb": 1785.3,
  "gasto_diario": 2767.3,
  "caloria_alvo": 2351.2,
  "macronutrientes": {
    "proteina_gramas": 136.8,
    "proteina_calorias": 547.2,
    "proteina_percentual": 23.3,
    "carboidrato_gramas": 264.8,
    "carboidrato_calorias": 1059.2,
    "carboidrato_percentual": 45.0,
    "gordura_gramas": 65.4,
    "gordura_calorias": 588.6,
    "gordura_percentual": 25.0
  },
  "recomendacoes": [
    "Aumentar ingestão de água: mínimo 3 litros por dia",
    "Distribuir proteína em 4-5 refeições para melhor absorção",
    "Priorizar fibras (alimentos integrais, frutas, verduras)",
    "Reduzir alimentos ultraprocessados e bebidas açucaradas",
    "Criar déficit calórico consistente com exercício regular"
  ],
  "data_calculo": "2025-01-20",
  "validade_dias": 30,
  "explicacao_calculo": "Cálculo realizado por fórmula de Harris-Benedict..."
}
```

### 2. Listar Assistentes Disponíveis

```bash
curl http://localhost:8080/api/v1/assistentes
```

### 3. Validar Dados para Cálculo

```bash
curl -X POST http://localhost:8080/api/v1/plano/validar \
  -H "Content-Type: application/json" \
  -d '{ ... }'
```

## 🔑 Tecnologias Utilizadas

| Tecnologia | Versão | Propósito |
|-----------|--------|----------|
| **Spring Boot** | 3.2.5 | Framework principal |
| **Java** | 21 | Linguagem |
| **LangChain4j** | 1.7.1-beta14 | Integração com LLMs |
| **Google Gemini** | 1.5 | Modelo de IA |
| **MapStruct** | 1.5.5 | Mapeamento de DTOs |
| **Swagger/OpenAPI** | 2.6.0 | Documentação da API |
| **Lombok** | Última | Redução de boilerplate |

## 🎓 Padrões e Conceitos Demonstrados

### 1. **Microserviço Independente**
- Não depende de entidades externas
- Recebe dados via DTOs
- Realiza cálculos internamente
- Retorna resultados estruturados

### 2. **Registry Pattern**
- `AssistantRegistry` para descoberta automática
- Facilita adição de novos assistentes
- Gerenciamento centralizado

### 3. **Strategy Pattern**
- Enums para estratégias de cálculo
- `ObjetivoNutricional` e `IntensidadeExercicio`
- Flexibilidade na distribuição de macros

### 4. **Factory Pattern**
- `AssistantConfig` para criação de beans
- Inicialização centralizada

### 5. **Mapper Pattern**
- MapStruct para conversão automática
- Separação clara entre modelos e DTOs
- Geração de código em tempo de compilação

### 6. **Tool Pattern (LangChain4j)**
- `PlanoAssistantTools` com métodos anotados `@Tool`
- LLM pode chamar ferramentas automaticamente
- Integração entre IA e lógica de negócio

## 📋 Configuração

### Variáveis de Ambiente

```bash
# Desenvolvimento
GEMINI_API_KEY=sua-chave-dev
GEMINI_MODEL=gemini-1.5-flash
SPRING_PROFILES_ACTIVE=local

# Produção
GEMINI_API_KEY=sua-chave-prod
GEMINI_MODEL=gemini-1.5-pro
SPRING_PROFILES_ACTIVE=prod
PORT=8080
```

### Properties Locais (application.properties)

```properties
spring.application.name=spring-ai-gemini-langchain4j-study
spring.profiles.active=local
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
```

## 🚧 Melhorias Futuras

- [ ] **Integração com Database Local**
  - Persistir planos calculados em PostgreSQL/MySQL
  - Histórico de cálculos por paciente
  - Versionamento de planos

- [ ] **Autenticação e Autorização**
  - Implementar Spring Security
  - JWT tokens para API
  - Controle de acesso por papel (ADMIN, NUTRICIONISTA, PACIENTE)

- [ ] **Múltiplos Assistentes**
  - AssistenteNutricional (atual)
  - AssistentePacientes (gestão de pacientes)
  - AssistenteRelatórios (análise de dados)
  - AssistenteFórum (respostas a perguntas)

- [ ] **Tools Avançadas**
  - Busca de alimentos no banco de dados
  - Cálculo de IMC automático
  - Geração de cardápios
  - Análise de histórico de peso

- [ ] **Frontend Web (Angular 19)**
  - Dashboard de planos
  - Formulário de cadastro de pacientes
  - Visualização de gráficos
  - Exportação para PDF



## 📚 Recursos Adicionais

- [LangChain4j Documentation](https://docs.langchain4j.dev/)
- [Google Gemini API](https://ai.google.dev/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MapStruct Guide](https://mapstruct.org/)
- [OpenAPI/Swagger](https://swagger.io/)

## 💡 Conceitos Aprendidos

Este projeto demonstra:
- Integração prática de LLMs em aplicações Spring
- Arquitetura de microserviços independentes
- Padrões de design em Java
- Processamento de linguagem natural
- Uso eficiente de DTOs e mappers
- Documentação automática de APIs
- Boas práticas de organização de código

## 👨‍💻 Autor

**César Augusto**
- Email: cesar.augusto.rj1@gmail.com
- Portfolio: https://portfolio.cesaravb.com.br/

---

⭐ Se este projeto foi útil, considere dar uma estrela no GitHub!