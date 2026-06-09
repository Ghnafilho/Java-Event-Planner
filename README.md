# Java Event Planner

Aplicação desktop de gerenciamento de eventos e calendário, desenvolvida em Java com Swing.  
Projeto acadêmico da disciplina de **Programação Orientada a Objetos**.

---

## Funcionalidades

- Visualização mensal do calendário com dias destacados quando há eventos
- Criação, edição e exclusão de eventos
- Suporte a eventos recorrentes (diário, semanal e mensal)
- Ao editar ou excluir um recorrente, escolha entre alterar só aquela ocorrência ou todas as futuras
- Lembretes automáticos via pop-up enquanto o programa está aberto
- Adição de participantes (nome e e-mail) a cada evento
- Busca de eventos por palavra-chave
- Exportação dos eventos de um dia para arquivo `.txt`
- Persistência automática em arquivo local — os dados são carregados ao abrir o programa
- Tratamento de erros com mensagens claras, sem expor stack traces ao usuário

---

## Estrutura do Projeto

```
planner/
├── Main.java                        # Ponto de entrada da aplicação
├── controller/
│   ├── EventController.java         # Lógica de negócio (CRUD, busca, exportação)
│   └── ReminderThread.java          # Thread de lembretes em segundo plano
├── model/
│   ├── Event.java                   # Modelo base de evento
│   ├── RecurringEvent.java          # Evento recorrente (herda de Event)
│   ├── Attendee.java                # Participante do evento
│   └── DataStorage.java             # Leitura e escrita em arquivo
├── view/
│   ├── MainFrame.java               # Janela principal
│   └── CalendarPanel.java           # Grade visual do calendário
└── exception/
    └── InvalidEventException.java   # Exceção customizada de validação
```

---

## Conceitos de POO Aplicados

| Conceito | Onde é aplicado |
|---|---|
| **Herança** | `RecurringEvent` estende `Event` |
| **Polimorfismo** | `toCSV()` sobrescrito em `RecurringEvent` |
| **Encapsulamento** | Atributos privados com getters/setters em todas as classes |
| **Composição** | `Event` contém uma lista de `Attendee` |
| **Exceção customizada** | `InvalidEventException` para validação de entradas |
| **Thread** | `ReminderThread` como daemon thread em paralelo |
| **Enum** | `RecurrenceType` para tipos de recorrência |

---

## Como Compilar e Rodar

### Pré-requisito
- Java 17 ou superior instalado

### No Linux/macOS
```bash
# Criar pasta de saída
mkdir out

# Compilar
javac -d out $(find planner -name "*.java")

# Rodar
java -cp out planner.Main
```

### No Windows (CMD)
```cmd
mkdir out

javac -d out planner\Main.java planner\model\*.java planner\controller\*.java planner\exception\*.java planner\view\*.java

java -cp out planner.Main
```

---

## Persistência de Dados

Os eventos são salvos automaticamente no arquivo `events_data.txt` na pasta raiz do projeto.  
O arquivo é carregado ao iniciar o programa. Caso esteja ausente ou corrompido, o programa inicia normalmente com a lista vazia.

---

## Arquivo de Dados — Formato

Cada linha representa um evento no formato:

```
título;dataHora;local;descrição;categoria;lembreteMinutos;participantes
```

Eventos recorrentes têm um sufixo adicional:
```
...;[REC:DAILY]
```
