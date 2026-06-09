package planner.view;

import planner.model.Event;
import planner.controller.EventController;
import planner.exception.InvalidEventException;
import planner.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class MainFrame extends JFrame {


    private final EventController controller;
    private final CalendarPanel calendarPanel;


    // Painel da direita
    private JList<Event>  listaEventos;
    private DefaultListModel<Event> modeloLista;
    private JTextArea     areaDetalhes;
    private LocalDate     dataSelecionada = LocalDate.now();


    public MainFrame(EventController controller) {
        this.controller   = controller;
        this.calendarPanel = new CalendarPanel(controller);


        setTitle("Event Planner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null); // centraliza na tela


        inicializarUI();
        atualizarListaEventos(dataSelecionada);
    }


    private void inicializarUI() {
        setLayout(new BorderLayout());


        add(criarBarraTopo(),    BorderLayout.NORTH);
        add(criarPainelCentral(), BorderLayout.CENTER);
    }


    // ── Barra do topo com botões ────────────────────────────────────


    private JPanel criarBarraTopo() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        barra.setBackground(new Color(248, 250, 252));
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));


        JButton btnNovo     = new JButton("+ Novo Evento");
        JButton btnHoje     = new JButton("Hoje");
        JButton btnExportar = new JButton("Exportar Dia");


        JTextField campoBusca = new JTextField(18);
        campoBusca.setToolTipText("Buscar eventos...");
        JButton btnBuscar = new JButton("🔍");


        btnNovo.addActionListener(e -> abrirFormularioNovo());
        btnHoje.addActionListener(e -> {
            dataSelecionada = LocalDate.now();
            calendarPanel.irParaHoje();
            atualizarListaEventos(dataSelecionada);
        });
        btnExportar.addActionListener(e -> exportarDia());
        btnBuscar.addActionListener(e -> buscarEventos(campoBusca.getText()));
        campoBusca.addActionListener(e -> buscarEventos(campoBusca.getText()));


        barra.add(btnNovo);
        barra.add(btnHoje);
        barra.add(new JSeparator(SwingConstants.VERTICAL));
        barra.add(new JLabel("Buscar:"));
        barra.add(campoBusca);
        barra.add(btnBuscar);
        barra.add(new JSeparator(SwingConstants.VERTICAL));
        barra.add(btnExportar);


        return barra;
    }


    // ── Painel central: calendário à esquerda, lista à direita ─────


    private JSplitPane criarPainelCentral() {
        // ESQUERDA: calendário
        calendarPanel.setAoClicarData(data -> {
            dataSelecionada = data;
            atualizarListaEventos(data);
        });


        // DIREITA: lista de eventos + detalhes
        JPanel painelDireita = criarPainelDireita();


        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                calendarPanel, painelDireita);
        split.setDividerLocation(500);
        split.setResizeWeight(0.4);
        return split;
    }


    private JPanel criarPainelDireita() {
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        // Lista de eventos do dia
        modeloLista = new DefaultListModel<>();
        listaEventos = new JList<>(modeloLista);
        listaEventos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaEventos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                exibirDetalhes(listaEventos.getSelectedValue());
            }
        });


        JScrollPane scrollLista = new JScrollPane(listaEventos);
        scrollLista.setPreferredSize(new Dimension(0, 200));


        // Área de detalhes
        areaDetalhes = new JTextArea(6, 35);
        areaDetalhes.setEditable(false);
        areaDetalhes.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollDetalhes = new JScrollPane(areaDetalhes);


        // Botões de ação
        JPanel botoes = new JPanel();
        botoes.setLayout((new BoxLayout(botoes, BoxLayout.Y_AXIS)));
        JButton btnEditar  = new JButton("✏ Editar");
        JButton btnDeletar = new JButton("🗑 Deletar");
        JButton btnAddAtt  = new JButton("👤 Participante");
        btnEditar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDeletar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAddAtt.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension tamanho = new Dimension(160, 35);

        btnEditar.setMaximumSize(tamanho);
        btnDeletar.setMaximumSize(tamanho);
        btnAddAtt.setMaximumSize(tamanho);


        btnEditar .addActionListener(e -> editarEventoSelecionado());
        btnDeletar.addActionListener(e -> deletarEventoSelecionado());
        btnAddAtt .addActionListener(e -> adicionarParticipante());


        botoes.add(btnEditar);
        botoes.add(Box.createVerticalStrut(10));
        botoes.add(btnDeletar);
        botoes.add(Box.createVerticalStrut(10));
        botoes.add(btnAddAtt);
        botoes.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));


        painel.add(new JLabel("Eventos do dia:"), BorderLayout.NORTH);
        painel.add(scrollLista,   BorderLayout.CENTER);
        JPanel scrollBotoes = new JPanel(new BorderLayout());
        scrollBotoes.add(scrollDetalhes, BorderLayout.CENTER);
        scrollBotoes.add(botoes,BorderLayout.EAST);
        painel.add(scrollBotoes, BorderLayout.SOUTH); 

        return painel;
    }


    // ── Atualizar lista da direita ──────────────────────────────────


    private void atualizarListaEventos(LocalDate data) {
        modeloLista.clear();
        List<Event> eventos = controller.getEventsForDate(data);
        for (Event e : eventos) {
            modeloLista.addElement(e);
        }
        areaDetalhes.setText("");
    }


    private void exibirDetalhes(Event event) {
        if (event == null) { areaDetalhes.setText(""); return; }


        StringBuilder sb = new StringBuilder();
        sb.append("Título:     ").append(event.getTitle()).append("\n");
        sb.append("Horário:    ").append(event.getFormattedDateTime()).append("\n");
        sb.append("Local:      ").append(event.getLocation()).append("\n");
        sb.append("Categoria:  ").append(event.getCategory()).append("\n");
        sb.append("Descrição:  ").append(event.getDescription()).append("\n");
        sb.append("Lembrete:   ").append(event.getReminderMinutesBefore()).append(" min antes\n");
        if (event instanceof RecurringEvent) {
            sb.append("Recorrência: ")
              .append(((RecurringEvent) event).getRecurrenceType()).append("\n");
        }


        if (!event.getAttendees().isEmpty()) {
            sb.append("\nParticipantes:\n");
            event.getAttendees().forEach(a -> sb.append("  • ").append(a).append("\n"));
        }


        areaDetalhes.setText(sb.toString());
    }


    // ── Formulário de criar/editar evento ──────────────────────────


    private void abrirFormularioNovo() {
        abrirFormulario(null);
    }


    private void editarEventoSelecionado() {
        Event selecionado = listaEventos.getSelectedValue();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um evento para editar.");
            return;
        }
        abrirFormularioEditavel(selecionado);
    }

    private void abrirFormularioEditavel(Event eventoExistente) {
        // Campos do formulário
        JTextField campoTitulo   = new JTextField(20);
        JTextField campoData     = new JTextField("Day/Month/Year Hour:Minutes", 20);
        campoData.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e){
                if(campoData.getText().equals("Day/Month/Year Hour:Minutes")){
                    campoData.setText("");
                } 
            }
            @Override
            public void focusLost(FocusEvent e){
                if(campoData.getText().isEmpty()){
                    campoData.setText("Day/Month/Year Hour:Minutes");
                }
            }
        });
        JTextField campoLocal    = new JTextField(20);
        JTextArea  campoDesc     = new JTextArea(3, 20);
        JComboBox<String> comboCat = new JComboBox<>(new String[]{
                "Reunião", "Aniversário", "Consulta", "Lembrete", "Outro"});
        JSpinner spinnerLembrete = new JSpinner(
                new SpinnerNumberModel(30, 0, 10080, 10));

        // Preenche se for edição
        if (eventoExistente != null) {
            campoTitulo.setText(eventoExistente.getTitle());
            campoData.setText(eventoExistente.getDateTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            campoLocal.setText(eventoExistente.getLocation());
            campoDesc.setText(eventoExistente.getDescription());
            spinnerLembrete.setValue(eventoExistente.getReminderMinutesBefore());
        }


        // Monta o painel do diálogo
        JPanel painel = new JPanel(new GridLayout(0, 2, 6, 6));
        painel.add(new JLabel("Título:*"));       painel.add(campoTitulo);
        painel.add(new JLabel("Data/Hora:*"));    painel.add(campoData);
        painel.add(new JLabel("Local:"));         painel.add(campoLocal);
        painel.add(new JLabel("Categoria:*"));    painel.add(comboCat);
        painel.add(new JLabel("Lembrete (min):")); painel.add(spinnerLembrete);
        painel.add(new JLabel("Descrição:"));     painel.add(new JScrollPane(campoDesc));


        int resultado = JOptionPane.showConfirmDialog(this, painel,
                eventoExistente == null ? "Novo Evento" : "Editar Evento",
                JOptionPane.OK_CANCEL_OPTION);


        if (resultado != JOptionPane.OK_OPTION) { return; }


        try {
            // Parseia a data digitada
            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime dt = LocalDateTime.parse(campoData.getText().trim(), fmt);


            String titulo    = campoTitulo.getText().trim();
            String local     = campoLocal.getText().trim();
            String desc      = campoDesc.getText().trim();
            String categoria = (String) comboCat.getSelectedItem();
            int lembrete     = (int) spinnerLembrete.getValue();
                // EDITAR
            if (eventoExistente instanceof RecurringEvent rec) {
                String[] opcoes = {"Só esta ocorrência", "Esta e todas as futuras", "Cancelar"};
                int escolha = JOptionPane.showOptionDialog(this,
                        "Este é um evento recorrente. O que deseja editar?",
                        "Editar evento recorrente",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, opcoes, opcoes[0]);

                if (escolha == 0) {
                    controller.updateEvent(rec, titulo, dt, local, desc, categoria, lembrete);
                } else if (escolha == 1) {
                    controller.updateFutureOccurrences(rec, titulo, dt, local, desc, categoria, lembrete);
                }
                // escolha == 2: não faz nada
            } else {
                controller.updateEvent(eventoExistente, titulo, dt, local, desc, categoria, lembrete);
            }
        calendarPanel.refresh();
        atualizarListaEventos(dataSelecionada);


        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Data inválida! Use o formato: dd/MM/yyyy HH:mm",
                    "Erro de validação", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidEventException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erro de validação", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void abrirFormulario(Event eventoExistente) {
        // Campos do formulário
        JTextField campoTitulo   = new JTextField(20);
        JTextField campoData     = new JTextField("Day/Month/Year Hour:Minutes", 20);
        campoData.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e){
                if(campoData.getText().equals("Day/Month/Year Hour:Minutes")){
                    campoData.setText("");
                } 
            }
            @Override
            public void focusLost(FocusEvent e){
                if(campoData.getText().isEmpty()){
                    campoData.setText("Day/Month/Year Hour:Minutes");
                }
            }
        });
        JTextField campoLocal    = new JTextField(20);
        JTextArea  campoDesc     = new JTextArea(3, 20);
        JComboBox<String> comboCat = new JComboBox<>(new String[]{
                "Reunião", "Aniversário", "Consulta", "Lembrete", "Outro"});
        JSpinner spinnerLembrete = new JSpinner(
                new SpinnerNumberModel(30, 0, 10080, 10));
        JSpinner spinnerRepeticoes = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));

        JComboBox<String> comboRec = new JComboBox<>(new String[]{
                "Sem recorrência", "Diária", "Semanal", "Mensal"});


        // Preenche se for edição
        if (eventoExistente != null) {
            campoTitulo.setText(eventoExistente.getTitle());
            campoData.setText(eventoExistente.getDateTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            campoLocal.setText(eventoExistente.getLocation());
            campoDesc.setText(eventoExistente.getDescription());
            comboCat.setSelectedItem(eventoExistente.getCategory());
            spinnerLembrete.setValue(eventoExistente.getReminderMinutesBefore());
        }


        // Monta o painel do diálogo
        JPanel painel = new JPanel(new GridLayout(0, 2, 6, 6));
        painel.add(new JLabel("Título:*"));       painel.add(campoTitulo);
        painel.add(new JLabel("Data/Hora:*"));    painel.add(campoData);
        painel.add(new JLabel("Local:"));         painel.add(campoLocal);
        painel.add(new JLabel("Categoria:*"));    painel.add(comboCat);
        painel.add(new JLabel("Lembrete (min):")); painel.add(spinnerLembrete);
        painel.add(new JLabel("Recorrência:"));   painel.add(comboRec);
        painel.add(new JLabel("Repetições (escolha entre 1 e 365):"));     painel.add(spinnerRepeticoes);
        painel.add(new JLabel("Descrição:"));     painel.add(new JScrollPane(campoDesc));


        int resultado = JOptionPane.showConfirmDialog(this, painel,
                eventoExistente == null ? "Novo Evento" : "Editar Evento",
                JOptionPane.OK_CANCEL_OPTION);


        if (resultado != JOptionPane.OK_OPTION) { return; }


        try {
            // Parseia a data digitada
            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime dt = LocalDateTime.parse(campoData.getText().trim(), fmt);


            String titulo    = campoTitulo.getText().trim();
            String local     = campoLocal.getText().trim();
            String desc      = campoDesc.getText().trim();
            String categoria = (String) comboCat.getSelectedItem();
            int lembrete     = (int) spinnerLembrete.getValue();
            String recStr    = (String) comboRec.getSelectedItem();
            int repeticoes = (int) spinnerRepeticoes.getValue();


            if (eventoExistente == null) {
                // CRIAR
                if (recStr.equals("Sem recorrência")) {
                    controller.createEvent(titulo, dt, local, desc, categoria, lembrete);
                } else {
                    RecurringEvent.RecurrenceType tipo = switch (recStr) {
                        case "Diária"   -> RecurringEvent.RecurrenceType.DAILY;
                        case "Semanal"  -> RecurringEvent.RecurrenceType.WEEKLY;
                        case "Mensal"   -> RecurringEvent.RecurrenceType.MONTHLY;
                        default         -> RecurringEvent.RecurrenceType.NONE;
                    };
                    controller.createRecurringEvent(titulo, dt, local, desc, categoria, lembrete, tipo, repeticoes);
                }
            } else {
                // EDITAR
                if (eventoExistente instanceof RecurringEvent rec) {
                    String[] opcoes = {"Só esta ocorrência", "Esta e todas as futuras", "Cancelar"};
                    int escolha = JOptionPane.showOptionDialog(this,
                            "Este é um evento recorrente. O que deseja editar?",
                            "Editar evento recorrente",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, opcoes, opcoes[0]);

                    if (escolha == 0) {
                        controller.updateEvent(rec, titulo, dt, local, desc, categoria, lembrete);
                    } else if (escolha == 1) {
                        controller.updateFutureOccurrences(rec, titulo, dt, local, desc, categoria, lembrete);
                    }
                    // escolha == 2: não faz nada
                } else {
                    controller.updateEvent(eventoExistente, titulo, dt, local, desc, categoria, lembrete);
                }
            }


            calendarPanel.refresh();
            atualizarListaEventos(dataSelecionada);


        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Data inválida! Use o formato: dd/MM/yyyy HH:mm",
                    "Erro de validação", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidEventException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erro de validação", JOptionPane.ERROR_MESSAGE);
        }
    }


    // ── Deletar evento ─────────────────────────────────────────────


    private void deletarEventoSelecionado() {
        Event selecionado = listaEventos.getSelectedValue();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um evento para deletar.");
            return;
        }


        if (selecionado instanceof RecurringEvent rec) {
            // Pergunta se quer deletar só este ou todos os futuros
            String[] opcoes = {"Só esta ocorrência", "Esta e todas as futuras", "Cancelar"};
            int escolha = JOptionPane.showOptionDialog(this,
                    "Este é um evento recorrente. O que deseja deletar?",
                    "Deletar evento recorrente",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, opcoes, opcoes[0]);


            if (escolha == 0) {
                controller.deleteEvent(rec);
            } else if (escolha == 1) {
                controller.deleteFutureOccurrences(rec);
            }
            // escolha == 2 ou fechou: não faz nada
        } else {
            int confirma = JOptionPane.showConfirmDialog(this,
                    "Deletar \"" + selecionado.getTitle() + "\"?",
                    "Confirmar exclusão", JOptionPane.YES_NO_OPTION);
            if (confirma == JOptionPane.YES_OPTION) {
                controller.deleteEvent(selecionado);
            }
        }


        calendarPanel.refresh();
        atualizarListaEventos(dataSelecionada);
    }


    // ── Adicionar participante ─────────────────────────────────────


    private void adicionarParticipante() {
        Event selecionado = listaEventos.getSelectedValue();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um evento primeiro.");
            return;
        }


        JTextField campoNome  = new JTextField(20);
        JTextField campoEmail = new JTextField(20);


        JPanel painel = new JPanel(new GridLayout(2, 2, 6, 6));
        painel.add(new JLabel("Nome:"));  painel.add(campoNome);
        painel.add(new JLabel("Email:")); painel.add(campoEmail);


        int resultado = JOptionPane.showConfirmDialog(this, painel,
                "Adicionar Participante", JOptionPane.OK_CANCEL_OPTION);


        if (resultado == JOptionPane.OK_OPTION) {
            try {
                String nome  = campoNome.getText();
                String email = campoEmail.getText();

                if (selecionado instanceof RecurringEvent rec) {

                    String[] opcoes = {
                        "Só este evento",
                        "Este e todos os futuros",
                        "Cancelar"
                    };

                    int escolha = JOptionPane.showOptionDialog(this,
                            "Este é um evento recorrente. Onde deseja adicionar o participante?",
                            "Evento recorrente",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            opcoes,
                            opcoes[0]);

                    if (escolha == 0) {
                        // só este
                        controller.addAttendee(rec, nome, email);

                    } else if (escolha == 1) {
                        // todos os futuros 
                        controller.addAttendeeToFutureOccurrences(rec, nome, email);
                    }


                } else {
                    // evento normal
                    controller.addAttendee(selecionado, nome, email);
                }

                exibirDetalhes(selecionado);

            } catch (InvalidEventException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // ── Buscar eventos ─────────────────────────────────────────────


    private void buscarEventos(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            atualizarListaEventos(dataSelecionada);
            return;
        }
        List<Event> resultados = controller.searchEvents(keyword.trim());
        modeloLista.clear();
        resultados.forEach(modeloLista::addElement);
        areaDetalhes.setText(resultados.size() + " resultado(s) encontrado(s) para: " + keyword);
    }


    // ── Exportar dia ───────────────────────────────────────────────


    private void exportarDia() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(
                "eventos_" + dataSelecionada + ".txt"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                controller.exportDayToFile(dataSelecionada,
                        chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Arquivo exportado com sucesso!");
            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao exportar: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
