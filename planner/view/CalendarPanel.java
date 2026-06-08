package planner.view;


import planner.controller.EventController;
import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Consumer;
import java.util.List;


public class CalendarPanel extends JPanel {


    private final EventController controller;
    private YearMonth mesAtual;
    private LocalDate dataSelecionada;
    private Consumer<LocalDate> aoClicarData; // callback para o MainFrame


    public CalendarPanel(EventController controller) {
        this.controller       = controller;
        this.mesAtual         = YearMonth.now();
        this.dataSelecionada  = LocalDate.now();
        setLayout(new BorderLayout());
        construir();
    }


    // O MainFrame chama isso para saber qual dia foi clicado
    public void setAoClicarData(Consumer<LocalDate> callback) {
        this.aoClicarData = callback;
    }


    // Reconstrói o painel (chamado ao trocar de mês ou ao adicionar evento)
    public void refresh() {
        removeAll();
        construir();
        revalidate();
        repaint();
    }


    public void irParaHoje() {
        mesAtual        = YearMonth.now();
        dataSelecionada = LocalDate.now();
        refresh();
    }


    // ── Monta o painel inteiro ──────────────────────────────────────


    private void construir() {
        add(criarBarraNavegacao(), BorderLayout.NORTH);
        add(criarGrade(),          BorderLayout.CENTER);
    }


    private JPanel criarBarraNavegacao() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(new Color(37, 99, 235));


        JButton anterior = new JButton("◀");
        JButton proximo  = new JButton("▶");


        anterior.addActionListener(e -> { mesAtual = mesAtual.minusMonths(1); refresh(); });
        proximo .addActionListener(e -> { mesAtual = mesAtual.plusMonths(1);  refresh(); });


        String nomeMes = mesAtual.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        JLabel titulo = new JLabel(
                nomeMes.substring(0,1).toUpperCase() + nomeMes.substring(1)
                + " " + mesAtual.getYear(),
                SwingConstants.CENTER
        );
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 15));


        nav.add(anterior, BorderLayout.WEST);
        nav.add(titulo,   BorderLayout.CENTER);
        nav.add(proximo,  BorderLayout.EAST);
        return nav;
    }


    private JPanel criarGrade() {
        JPanel container = new JPanel(new BorderLayout());


        // Cabeçalho: Dom Seg Ter...
        JPanel cabecalho = new JPanel(new GridLayout(1, 7));
        String[] dias = {"Dom","Seg","Ter","Qua","Qui","Sex","Sáb"};
        for (String d : dias) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            cabecalho.add(l);
        }


        // Grade dos dias
        JPanel grade = new JPanel(new GridLayout(0, 7, 2, 2));


        // Pegar quais dias do mês têm eventos
        List<LocalDate> comEventos = controller.getDatesWithEventsInMonth(
                mesAtual.getYear(), mesAtual.getMonthValue());


        // O dia 1 do mês começa em qual coluna?
        // getDayOfWeek().getValue() retorna: SEG=1 ... DOM=7
        // Queremos: DOM=0 ... SEG=1 ... SAB=6
        LocalDate primeiroDia = mesAtual.atDay(1);
        int colunaDia1 = primeiroDia.getDayOfWeek().getValue() % 7;


        // Preenche células vazias antes do dia 1
        for (int i = 0; i < colunaDia1; i++) {
            grade.add(new JPanel()); // célula vazia
        }


        // Preenche os dias do mês
        for (int dia = 1; dia <= mesAtual.lengthOfMonth(); dia++) {
            LocalDate data = mesAtual.atDay(dia);
            grade.add(criarCelulaDia(data, comEventos));
        }


        container.add(cabecalho, BorderLayout.NORTH);
        container.add(grade,     BorderLayout.CENTER);
        return container;
    }


    private JPanel criarCelulaDia(LocalDate data, List<LocalDate> comEventos) {
        JPanel celula = new JPanel(new BorderLayout());
        celula.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));


        boolean ehHoje      = data.equals(LocalDate.now());
        boolean ehSelecionado = data.equals(dataSelecionada);
        boolean temEvento   = comEventos.contains(data);


        JLabel numero = new JLabel(String.valueOf(data.getDayOfMonth()), SwingConstants.CENTER);
        numero.setFont(new Font("Segoe UI", ehHoje ? Font.BOLD : Font.PLAIN, 13));


        if (ehHoje) {
            celula.setBackground(new Color(37, 99, 235));
            numero.setForeground(Color.WHITE);
        } else if (ehSelecionado) {
            celula.setBackground(new Color(191, 219, 254));
        } else {
            celula.setBackground(Color.WHITE);
        }


        celula.add(numero, BorderLayout.CENTER);


        // Bolinha vermelha embaixo se tiver evento
        if (temEvento) {
            JPanel bolinha = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(new Color(220, 38, 38));
                    g.fillOval(getWidth()/2 - 3, 1, 6, 6);
                }
            };
            bolinha.setOpaque(false);
            bolinha.setPreferredSize(new Dimension(10, 10));
            celula.add(bolinha, BorderLayout.SOUTH);
        }


        // Ao clicar, seleciona o dia e avisa o MainFrame
        celula.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        celula.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dataSelecionada = data;
                refresh();
                if (aoClicarData != null) {
                    aoClicarData.accept(data); // avisa o MainFrame
                }
            }
        });


        return celula;
    }
}
