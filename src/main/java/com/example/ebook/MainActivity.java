package com.example.ebook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final String CONFIG_FILE_NAME = "Config"; // Имя файла настроек приложения
    private static final String ID_TOPIC = "idTopic"; // Название ключа для хранения ID-кода выбранной темы
    private static final String FULL_SIZE_FONT = "isFullSizeFont"; // Название ключа для хранения выбора крупного шрифта
    private static final String MY_SEARCH = "mySearch"; // Название ключа для хранения разрешения поиска
    private static final String HTML_ASSETS_DIR = "file:///android_asset/HTML/"; // Путь к html-страницам в ресурсах приложения
    private static final List<String> LIST_OF_TOPICS = new ArrayList<>();
    private static final HashMap<Integer, Integer> ID_TO_INDEX = new HashMap<>();
    private static final HashMap<Integer, Integer> INDEX_TO_ID = new HashMap<>();
    EditText searchText; // Поле для ввода искомого текста
    TextView searchCountText; // Поле для отображения сколько найдено фрагментов поиска
    ImageButton searchForwardButton, searchCloseButton, searchBackButton; // Кнопки навигации поиска
    RelativeLayout searchToolLayout;// Панель поиска
    FloatingActionButton searchButton;// Круглая кнопка поиска
    private NestedScrollView nestedScrollView;
    private NavigationView navigationView;
    private WebView webView; // Компонент для просмотра html-страниц
    private int idTopic; // ID-код выбранной темы
    private SharedPreferences sPref; // Переменная для работы с настройками программы
    private boolean isFullSizeFont = true; // Переменная признака выбора крупного шрифта с инициализацией
    private boolean mySearch = true;// Переменная признака разрешения поиска с инициализацией
    private int currentApiOS; // Переменная для определения версии Android пользователя

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация переменных для поиска
        searchForwardButton = findViewById(R.id.searchForwardButton);
        searchBackButton = findViewById(R.id.searchBackButton);
        searchCloseButton = findViewById(R.id.searchCloseButton);
        searchText = findViewById(R.id.searchText);
        searchCountText = findViewById(R.id.searchCountText);
        searchButton = findViewById(R.id.searchButton);
        searchToolLayout = findViewById(R.id.searchToolLayout);

        // Определение API версии Android
        currentApiOS = android.os.Build.VERSION.SDK_INT;

        // Инициализация переменной настроек программы
        sPref = getSharedPreferences(CONFIG_FILE_NAME, MODE_PRIVATE);

        // Обработка переворачивания экрана и начальная инициализация выбранной темы (ID_TOPIC) в приложении
        if (savedInstanceState != null) {
            // Вторичное создание окна после переворачивания экрана
            isFullSizeFont = savedInstanceState.getBoolean(FULL_SIZE_FONT, isFullSizeFont);
            mySearch = savedInstanceState.getBoolean(MY_SEARCH, mySearch);
            idTopic = savedInstanceState.getInt(ID_TOPIC, R.id.page0);
        } else {
            // Первый запуск программы до переворачивания экрана
            // Чтение данных с настроек программы
            isFullSizeFont = sPref.getBoolean(FULL_SIZE_FONT, isFullSizeFont);
            mySearch = sPref.getBoolean(MY_SEARCH, mySearch);
            idTopic = sPref.getInt(ID_TOPIC, R.id.page0);
        }

        // Включение/отключение кнопки поиска в зависимости от настроек пользователя
        if (mySearch) {
            searchButton.setVisibility(View.VISIBLE);
        } else {
            searchButton.setVisibility(View.GONE);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Устанавливаем выбранный пункт меню
        try {
            navigationView.setCheckedItem(idTopic);
        } catch (Exception ignore) {
        }

        nestedScrollView = findViewById(R.id.nestedScrollView);

        // Поиск компонента для отображения html-страниц
        webView = findViewById(R.id.webView);

        // ------------- ВАЖНАЯ СЕКЦИЯ ! ---------------------------------
        // Открытие ссылок внутри компонента без вызова внешнего браузера!
        // Чтоб работали такие ссылки в HTML для перехода из страницы в страницу:
        //</div>
        //<p class="msonormal">&nbsp;<a href="/android_asset/HTML/LEC01/lec01.htm">&gt;&gt;&gt; В НАЧАЛО  &gt;&gt;&gt;</a></p>
        //</body>
        //</html>
        webView.setWebViewClient(new WebViewClient()); // ЭТО ОБЯЗАТЕЛЬНАЯ СТРОКА !!!

        // Патч для HTML чтобы не было глюков! ЭТО ОБЯЗАТЕЛЬНЫЙ КОД !!!
        try {
            Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
            m.invoke(null);
        } catch (Exception ignored) {
        }
        // ---------------------------------------------------------------


        initWebView(isFullSizeFont);

        LIST_OF_TOPICS.add("weather/0.htm");
        LIST_OF_TOPICS.add("meteorology/1.htm");
        LIST_OF_TOPICS.add("causes/2.htm");
        LIST_OF_TOPICS.add("forecasting/3.htm");
        ID_TO_INDEX.put(R.id.page0, 0);
        ID_TO_INDEX.put(R.id.page1, 1);
        ID_TO_INDEX.put(R.id.page2, 2);
        ID_TO_INDEX.put(R.id.page3, 3);
        INDEX_TO_ID.put(0, R.id.page0);
        INDEX_TO_ID.put(1, R.id.page1);
        INDEX_TO_ID.put(2, R.id.page2);
        INDEX_TO_ID.put(3, R.id.page3);

        //Инициализация начала просмотра html-страниц
        onNavigationItemSelected(null);

        // Обработчик кнопки Поиск
        searchButton.setOnClickListener(view -> {
            searchCountText.setText("");
            searchToolLayout.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.GONE);
            searchText.requestFocus();
        });

        // Обработчик кнопки Поиск Вперед
        searchForwardButton.setOnClickListener(v -> webView.findNext(true));

        // Обработчик кнопки Поиск Назад
        searchBackButton.setOnClickListener(v -> webView.findNext(false));

        // Обработчик нажатий кнопок в окошке поиска
        searchText.setOnKeyListener((v, keyCode, event) -> {
            // Если нажата клавиша Enter
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && ((keyCode == KeyEvent.KEYCODE_ENTER))) {
                // Скрываем клавиатуру
                hideSoftInput();
                // Ищем нужный текст в webView
                webView.findAll(searchText.getText().toString());
                // Активируем возможность отображения найденного теккста в webView
                try {
                    Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                    m.invoke(webView, true);
                } catch (Exception ignored) {
                }
            }
            return false;
        });

        // Обработчик поиска в WebView
        webView.setFindListener(new WebView.FindListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
                searchCountText.setText("");
                if (numberOfMatches > 0) {
                    searchCountText.setText(String.format("%d %s %d", activeMatchOrdinal + 1, "из", numberOfMatches));
                } else {
                    searchCountText.setText("Введенный текст не найден");
                }
            }
        });

        // Обработчик кнопки закрытия поиска
        searchCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.clearMatches();
                searchText.setText("");
                searchToolLayout.setVisibility(View.GONE);
                if (mySearch) {
                    searchButton.setVisibility(View.VISIBLE);
                }
                hideSoftInput();
            }
        });

        searchCloseButton.performClick();
    }

    public void btnBackOnClick(View view) {
        try {
            int topicIndex = ID_TO_INDEX.get(idTopic) - 1;
            String topicLink = HTML_ASSETS_DIR + LIST_OF_TOPICS.get(topicIndex);
            webView.loadUrl(topicLink);
            idTopic = INDEX_TO_ID.get(topicIndex);
            nestedScrollView.scrollTo(0, 0);
            navigationView.setCheckedItem(idTopic);
        } catch (Exception e) {
        }
    }

    public void btnNextOnClick(View view) {
        try {
            int topicIndex = ID_TO_INDEX.get(idTopic) + 1;
            String topicLink = HTML_ASSETS_DIR + LIST_OF_TOPICS.get(topicIndex);
            webView.loadUrl(topicLink);
            idTopic = INDEX_TO_ID.get(topicIndex);
            nestedScrollView.scrollTo(0, 0);
            navigationView.setCheckedItem(idTopic);
        } catch (Exception e) {
        }
    }

    // Сохранение данных в буфер при переворачивании экрана
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(FULL_SIZE_FONT, isFullSizeFont); // Сохраняем крупный ли шрифт
        savedInstanceState.putBoolean(MY_SEARCH, mySearch); // Сохраняем разрешение поиска
        savedInstanceState.putInt(ID_TOPIC, idTopic); // Сохраняем ID текущей темы
        super.onSaveInstanceState(savedInstanceState);
    }

    // Метод при закрытии окна
    @Override
    protected void onStop() {
        super.onStop();
        // Сохранение настроек программы в файл настроек
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(FULL_SIZE_FONT, isFullSizeFont);
        ed.putBoolean(MY_SEARCH, mySearch);
        ed.putInt(ID_TOPIC, idTopic);
        ed.apply();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Создание меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Отключение пункта крупного шрифта для старых версий Android из-за ограничений их WebView
        MenuItem fullSizeItem = menu.findItem(R.id.full_size_font);
        try {
            if (currentApiOS < android.os.Build.VERSION_CODES.LOLLIPOP) {
                isFullSizeFont = true;
                fullSizeItem.setCheckable(false);
                fullSizeItem.setEnabled(false);
                fullSizeItem.setChecked(true);
            } else {
                fullSizeItem.setCheckable(true);
                fullSizeItem.setChecked(isFullSizeFont);
            }
        } catch (Exception ignored) {
        }

        // Отключение/включение пункта поиска
        MenuItem mySearchItem = menu.findItem(R.id.mySearch);
        try {
            mySearchItem.setChecked(mySearch);
        } catch (Exception ignored) {
        }

        return true;
    }

    // Обработка верхнего правого меню
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                finish();
                return true;
            case R.id.full_size_font:
                isFullSizeFont = !item.isChecked();
                item.setChecked(isFullSizeFont);
                initWebView(isFullSizeFont);
                return true;
            case R.id.mySearch:
                mySearch = !item.isChecked();
                item.setChecked(mySearch);
                if (mySearch) {
                    searchButton.setVisibility(View.VISIBLE);
                } else {
                    searchButton.setVisibility(View.GONE);
                    searchCloseButton.callOnClick();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id;

        if (item == null) { // Вызывается при начальном открытии окна
            id = idTopic;
        } else { // Вызывается при выборе темы из меню тем
            id = item.getItemId();
        }

        // Блок выбора переходов
        if (id == R.id.nav_viewNow) { // Переход на внешнюю html-ссылку и внешний браузер
            openLinkExternally("https://www.gismeteo.kz/weather-pavlodar-5174/now");
        }
        if (id == R.id.nav_viewToday) {
            openLinkExternally("https://www.gismeteo.kz/weather-pavlodar-5174/");
        }
        if (id == R.id.nav_viewTomorrow) {
            openLinkExternally("https://www.gismeteo.kz/weather-pavlodar-5174/tomorrow/");
        }
        if (id == R.id.nav_viewThreeDays) {
            openLinkExternally("https://www.gismeteo.kz/weather-pavlodar-5174/3-days/");
        }
        if (id == R.id.nav_viewWeekly) {
            openLinkExternally("https://www.gismeteo.kz/weather-pavlodar-5174/weekly/");
        }
        if (id == R.id.nav_viewTwoWeeks) {
            openLinkExternally("https://www.gismeteo.kz/weather-pavlodar-5174/2-weeks/");
        }
        if (id == R.id.nav_viewMonthly) {
            openLinkExternally("https://www.gismeteo.kz/weather-pavlodar-5174/month/");
        }
        if (id == R.id.nav_send) {
            sendMail("email@example.com", "Предложения по приложению", "Здравствуйте!");
        } else {
            idTopic = id;
            int topicIndex = ID_TO_INDEX.get(id);
            webView.loadUrl(HTML_ASSETS_DIR + LIST_OF_TOPICS.get(topicIndex));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Инициализация компонента просмотра html-страниц
    private void openLinkExternally(String uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        try {
            startActivity(Intent.createChooser(intent, "Перейти к чтению..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    // Посылка письма автору
    private void sendMail(String email, String subject, String text) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, text);
        try {
            startActivity(Intent.createChooser(i, "Отправление письма автору"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "Нет установленного почтового клиента", Toast.LENGTH_SHORT).show();
        }
    }

    // Инициализация компонента просмотра html-страниц и размера шрифта
    private void initWebView(boolean isFullScreen) {
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        if ((currentApiOS >= android.os.Build.VERSION_CODES.LOLLIPOP) && (!isFullScreen)) {
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setSupportZoom(true);
        } else {
            webView.getSettings().setLoadWithOverviewMode(false);
            webView.getSettings().setUseWideViewPort(false);
            webView.getSettings().setBuiltInZoomControls(false);
            webView.getSettings().setSupportZoom(false);
        }
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setScrollbarFadingEnabled(false);
    }

    // Скрываем клавиатуру
    private void hideSoftInput() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception ignored) {
        }
    }


}

