
#include <ESP8266WebServer.h>
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>

const char* ssid = "xxxxxxx";
const char* password = "xxxxxx";

ESP8266WebServer server ( 80);

LiquidCrystal_I2C lcd(0x27,16,2);

#define Max_Length_Tabla 20  // máximo nº de registro que mostraremos en la tabla display

//Estructura que usaremos para representar los datos en el display, que obtenemos del brain4it
struct registro_tabla
  {
    int fila;    
    int colX1;
    int colX2;    
    String mensaje;    
    int delayScroll;
    int scroll;
    int modo;    
  };

// en tabla_display[], almacenaremos la estructura de cada línea que vamos a mostrar en el display
registro_tabla tabla_display[Max_Length_Tabla];

int fin_tabla_display = 0;      // contendrá el nº de elementos real que tenemos en la tabla_display
int longitud_max_mensaje = 0;    // contendrá las posiciones del bucle para mostrar el mensaje en el display
int modo_visualizar = 1;        // modo visualización tabla, por defecto 1.
bool datos_recibidos = false;
int salir_bucle = 0;

/*
  La tabla que recibe viene como un String y tendrá la siguiente estructura:

    "[[fila, colX1, colX2, Mensaje, delay, scroll, Modo], ... [fila, colX1, colX2, Mensaje, delay, scroll, Modo]]"

  Donde:
    - fila   =  valor (int) de la fila del display de 0 a N (Según las que tenga el dispositivo).
    - colX1  =  valor (int) de la columna X1 de la fila del display, indica la posición de inicio donde visualizaremos el mensaje. 
    - colX2  =  valor (int) de la columna X2 de la fila del display, indica la posición de fin donde visualizaremos el mensaje. . Si la longitud del Mensaje 
                es mayor que su lugar de visualización (colX1 a colX2), el mensaje tendrá scroll, de lo contrario no tendrá.
    - mensaje=  valor string del texto a visualizar en la fila y columnas (fila, colX1, colX2) indicados. Todos los textos pasados, tendrán la misma longitud y terminarán 
                con un espacio.      
    - delayScroll  =  valor (int) para el retardo (delay(X)) para visualizar el mensaje cuando hay scroll. Si no tiene scroll, puede ser 0.
    - scroll =  valor (int 0/1) indica si tiene o no scroll ese mensaje.
    - modo   =  valor (int) de 1 a 4, que determinará el modo en el que vamos a mostrar los mensajes en el display. Hemos definido estos modos de trabajo:
                - Modo 1 = Visualiza el contenido de la tabla de mensajes en 1 sola línea del display (hace uso de las 2 líneas para ver el mensaje mejor).
                - Modo 2 = Visualiza el contenido de la tabla de mensajes en 2 líneas del display.
                - Modo 3 = Visualiza el contenido de la tabla de mensajes en 2 líneas del display. la 1ª línea la divide en 2 partes, la 2ª línea entera.
                - Modo 4 = Visualiza el contenido de la tabla de mensajes en 2 líneas del display. dividiendo en 2 partes cada línea.    

*/

// carga a las variables los valores de la cadena recibida como parámetro
// cada vez que entremos en esta función,hemos recibido datos nuevos de la agenda 
void ver_recibido(){
  String cad;
 
  //eliminamos el [ y el último ] de la cadena recibida, solo deja los [ y ] de cada registro de la tabla...    
  cad = server.arg(0);  
  cad.remove(cad.length()-1,1);
  cad.remove(0,1);     
  
  elementos_tabla(cad);     // carga los datos recibidos en sus tablas
  
  longitud_max_mensaje = tabla_display[0].mensaje.length()+1;
  modo_visualizar = tabla_display[0].modo;  
}

// coloca cada parte de la cadena en su lugar dentro de la estructura tabla_display
void elementos_tabla(String cadena){
  int elem_registros_tabla =0;
  int pos_inicio_abre[20];
  int pos_fin_cierra[20];
  String tabla_elem [20];
  int l=0, y=0, x=0;
  String letra ="";
 
  //leemos cuantos elementos recibimos en la cadena de brain4it
  for (int i =0; i < cadena.length(); i++){
    
    letra = cadena.substring(i,1+i);  //miramos si el caracter que leemos es un '[' o ']' para contar
    if (letra== "[") {
      pos_inicio_abre[y] = i;
      y++;
    }else if (letra=="]"){
      pos_fin_cierra[x] = i;
      x++;
    }
    l++;
  }
  
  //guardamos cada subcadena correspondiente a cada elemento de la tabla recibida
  for (int i=0; i < y; i++){    
    tabla_elem[i] = cadena.substring(pos_inicio_abre[i]+1, pos_fin_cierra[i]);           
    tabla_display [i] = llenar_tabla_display(tabla_elem[i]);    // cargamos los elementos de la tabla display con su estructura de datos
  }
  
  fin_tabla_display = y;      // elementos reales que hay en la tabla display
}

//llenamos la estructura de datos del registro de la tabla con cada parte de la cadena que le corresponde
registro_tabla llenar_tabla_display(String cadena_registro) {
  int len = cadena_registro.length();
  int pos1;
    
  //  creamos una var con la estructura de registro_tabla para uso temporal. La cadena recibida contiene toda la información según este formato:
  //  "fila, colX1, colX2, Mensaje, delay, scroll, Modo"
  
  registro_tabla _temporal; 
  
  pos1 = cadena_registro.indexOf(",");
  _temporal.fila = cadena_registro.substring(0,pos1).toInt();  // fila donde mostraremos el mensaje
  
  cadena_registro = cadena_registro.substring(pos1+1);
  pos1 = cadena_registro.indexOf(",");
  
  _temporal.colX1 = cadena_registro.substring(0,pos1).toInt();  // coordenada colX1 inicio del scroll
  
  cadena_registro = cadena_registro.substring(pos1+1);  
  pos1 = cadena_registro.indexOf(",");
  
  _temporal.colX2 = cadena_registro.substring(0,pos1).toInt();  // coordenada colX2 del scroll    
  
  cadena_registro = cadena_registro.substring(pos1+1);          
  pos1 = cadena_registro.indexOf(",");
  
  _temporal.mensaje= cadena_registro.substring(1,pos1-1);       // mensaje de texto a mostrar entre colx1 y colx2
  
  cadena_registro = cadena_registro.substring(pos1+1);  
  pos1 = cadena_registro.indexOf(",");
  
  _temporal.delayScroll= cadena_registro.substring(0,pos1).toInt(); // coordenada y inicio de la estructura 

  cadena_registro = cadena_registro.substring(pos1+1);  
  pos1 = cadena_registro.indexOf(",");

  _temporal.scroll = cadena_registro.substring(0,pos1).toInt();   // tiene o no scroll la línea del mensaje

  cadena_registro = cadena_registro.substring(pos1+1);  
  pos1 = cadena_registro.indexOf(",");

  _temporal.modo=   cadena_registro.substring(0,pos1).toInt(); // modo según el que vamos a tratar la información en el display
      
  return _temporal;    // retorna la estructura registro_tabla de los datos del String procesado...
}

//Esta función es llamada cuando recibe algún dato de brain4it y carga esos datos en las tablas para despúes mostrarlos
void cargarVariables(){  
  
  // ya hemos recibido algunos datos, los podemos ver en el display, guardamos valores para el control del bucle
  datos_recibidos = true;
  //al inicio vale 0, se suma 1 y saldrá del bucle cuando valga > 1, una vez salga del bucle restamos 1.
  salir_bucle ++;
  ver_recibido();  
  
  delay(100);  
  server.send ( 200, "text/html", "Ok"); 
}

void setup ( void ) {
  
  Serial.begin(115200);  
  
  //inicializa el display
  lcd.init(); 
  lcd.backlight();
  lcd.setCursor(0,0);    	
  lcd.print("Conectando a: ");
  lcd.setCursor(0,1);      
  lcd.print(ssid);

  //se conecta a la wifi
	WiFi.mode ( WIFI_STA );
  WiFi.begin ( ssid, password );  
   
  while (WiFi.status() != WL_CONNECTED) {     
    lcd.setCursor(13,1);   
    lcd.print(" . ");      
    delay ( 750 );                     
  }   

  //definimos la función que llamaremos al recibir datos del brain4it
  server.on("/ver",HTTP_POST, cargarVariables );    	  
	server.begin();
   
  lcd.clear();  
	lcd.println ( "HTTP server IP." );
  lcd.setCursor(0,1);
  lcd.print ( WiFi.localIP() );      
}

void loop ( void ) {
  // controla que se recibán datos del cliente vía wifi  
  server.handleClient();   

  // hasta que no recibamos datos, no los visualizaremos.
  //procesamos toda la tabla, según el modo de visualizar que le pasemos(1,2,3,4) 
  if (datos_recibidos) {  
    switch (modo_visualizar)
    {
      case 1: visualizar_modo1();
                    break;
      case 2: visualizar_modo2();
                    break;
      case 3: visualizar_modo3();
                    break;
      case 4: visualizar_modo4();
                    break;
    }    
  }
    
  delay(50);
}

// Modo 1: visualiza la tabla_display mensaje a mensaje (el mismo se representa en las 2 filas del display haciendo scroll entre ambas líneas
void visualizar_modo1()
{
  
  String c1,c2;
  int cont_elem_tabla = 0; 
  int long_subcad1, y;

  Serial.println("entramos en visualizar 1 : " + String(modo_visualizar));
  
  while ( (cont_elem_tabla < fin_tabla_display) and (salir_bucle < 2)) 
  {
    borrar_display();
  
    long_subcad1 = tabla_display[cont_elem_tabla].colX2 - tabla_display[cont_elem_tabla].colX1;
       
    for (int x = 0; x < (longitud_max_mensaje/2); x++) 
      {
        // si no quiere scroll en la línea 1
        y = (tabla_display[cont_elem_tabla].scroll == 1 ? x : 0);       
        c1 = tabla_display[cont_elem_tabla].mensaje.substring(0+y, long_subcad1 + y);
        c2 = tabla_display[cont_elem_tabla].mensaje.substring(long_subcad1+y, long_subcad1+long_subcad1 + y);                    
        
        imprime_display (tabla_display[cont_elem_tabla].colX1, tabla_display[cont_elem_tabla].fila, c1);    
        imprime_display (tabla_display[cont_elem_tabla].colX1, tabla_display[cont_elem_tabla].fila+1, c2);    
        delay(tabla_display[cont_elem_tabla].delayScroll);        
        
        if(salir_bucle > 1){
          borrar_display();   // hemos recibido nuevos datos del brain4it, salimos del bucle
          break;
        }
      }  
    cont_elem_tabla ++;
    delay(1000);           
  }
  salir_bucle = 1;
}

//modo visualizar 2 = se visualiza en las 2 líneas del display, un elemento por línea
void visualizar_modo2()
{
    
  String c1,c2;
  int cont_elem_tabla = 0; 
  int long_subcad1, long_subcad2,y;  

  Serial.println("entramos en visualizar 2 : " + String(modo_visualizar));
  
  while ( (cont_elem_tabla < fin_tabla_display) and (salir_bucle < 2)) 
  {
    borrar_display();
    
    long_subcad1 = tabla_display[cont_elem_tabla].colX2 - tabla_display[cont_elem_tabla].colX1;
    long_subcad2 = tabla_display[cont_elem_tabla+1].colX2 - tabla_display[cont_elem_tabla+1].colX1;
   
    for (int x = 0; x < longitud_max_mensaje; x++) 
      {
        // si no quiere scroll en la línea 1
        y = (tabla_display[cont_elem_tabla].scroll == 1 ? x : 0);
        c1 = tabla_display[cont_elem_tabla].mensaje.substring(0+y, long_subcad1 + y);
        // si no quiere scroll en la línea 2
        y = (tabla_display[cont_elem_tabla+1].scroll == 1 ? x : 0);
        c2 = tabla_display[cont_elem_tabla+1].mensaje.substring(0+y, long_subcad2 + y);
    
        imprime_display (tabla_display[cont_elem_tabla].colX1, tabla_display[cont_elem_tabla].fila, c1);
    
        imprime_display (tabla_display[cont_elem_tabla+1].colX1, tabla_display[cont_elem_tabla+1].fila, c2);
      
        delay(tabla_display[cont_elem_tabla].delayScroll);        
        
        if(salir_bucle > 1){
          borrar_display();     // hemos recibido nuevos datos del brain4it, salimos del bucle
          break;
        }
      }
    cont_elem_tabla +=2;               
    delay(1000);
  }
  salir_bucle = 1;
}

//modo visualizar 3 = se visualiza en las 2 líneas del display, la 1º entera y la 2º dividida en 2 trozos (0-7, 8-16)
void visualizar_modo3()
{
    
  String c1,c2,c3;
  int cont_elem_tabla = 0; 
  int long_subcad1, long_subcad2, long_subcad3, y;

  Serial.println("entramos en visualizar 3 : " + String(modo_visualizar));
  
  while ( (cont_elem_tabla < fin_tabla_display) and (salir_bucle < 2)) 
  {
    borrar_display();
     
    long_subcad1 = tabla_display[cont_elem_tabla].colX2 - tabla_display[cont_elem_tabla].colX1;
    long_subcad2 = tabla_display[cont_elem_tabla+1].colX2 - tabla_display[cont_elem_tabla+1].colX1;
    long_subcad3 = tabla_display[cont_elem_tabla+2].colX2 - tabla_display[cont_elem_tabla+2].colX1;

    Serial.println(" cad1 > " + String(long_subcad1)+" cad2 > " + String(long_subcad2) + " cad3 >" + String(long_subcad3));
    
    for (int x = 0; x < longitud_max_mensaje; x++) 
      {
        // si no quiere scroll en la línea 1
        y = (tabla_display[cont_elem_tabla].scroll == 1 ? x : 0);
        c1 = tabla_display[cont_elem_tabla].mensaje.substring(0+y, long_subcad1 + y);
        // si no quiere scroll en la línea 2
        y = (tabla_display[cont_elem_tabla+1].scroll == 1 ? x : 0);
        c2 = tabla_display[cont_elem_tabla+1].mensaje.substring(0+y, long_subcad2 + y);
        // si no quiere scroll en la línea 3
        y = (tabla_display[cont_elem_tabla+2].scroll == 1 ? x : 0);
        c3 = tabla_display[cont_elem_tabla+2].mensaje.substring(0+y, long_subcad3 + y);       
    
        imprime_display (tabla_display[cont_elem_tabla].colX1, tabla_display[cont_elem_tabla].fila, c1);
        
        imprime_display (tabla_display[cont_elem_tabla+1].colX1, tabla_display[cont_elem_tabla+1].fila, c2);
        
        imprime_display (tabla_display[cont_elem_tabla+2].colX1, tabla_display[cont_elem_tabla+2].fila, c3);     
      
        delay(tabla_display[cont_elem_tabla].delayScroll);
        
        if(salir_bucle > 1){
          borrar_display();   // hemos recibido nuevos datos del brain4it, salimos del bucle
          break;
        }
      }
    cont_elem_tabla +=3;  
    delay(1000);             
  }
  salir_bucle = 1;
}

////modo visualizar 4 = se visualiza en las 2 líneas del display, cada línea dividida en 2 trozos (0-7, 8-16)
void visualizar_modo4()
{
    
  String c1,c2,c3,c4;
  int cont_elem_tabla = 0; 
  int long_subcad1, long_subcad2, long_subcad3, long_subcad4, y;

  Serial.println("entramos en visualizar 4 : " + String(modo_visualizar));
  
  while ( (cont_elem_tabla < fin_tabla_display) and (salir_bucle < 2)) 
  {
    borrar_display();
    
    long_subcad1 = tabla_display[cont_elem_tabla].colX2 - tabla_display[cont_elem_tabla].colX1;
    long_subcad2 = tabla_display[cont_elem_tabla+1].colX2 - tabla_display[cont_elem_tabla+1].colX1;
    long_subcad3 = tabla_display[cont_elem_tabla+2].colX2 - tabla_display[cont_elem_tabla+2].colX1;
    long_subcad4 = tabla_display[cont_elem_tabla+3].colX2 - tabla_display[cont_elem_tabla+3].colX1;
    
    for (int x = 0; x < longitud_max_mensaje; x++) 
      {
        // si no quiere scroll en la línea 1
        y = (tabla_display[cont_elem_tabla].scroll == 1 ? x : 0);
        c1 = tabla_display[cont_elem_tabla].mensaje.substring(0+y, long_subcad1 + y);
        // si no quiere scroll en la línea 2
        y = (tabla_display[cont_elem_tabla+1].scroll == 1 ? x : 0);
        c2 = tabla_display[cont_elem_tabla+1].mensaje.substring(0+y, long_subcad2 + y);
        // si no quiere scroll en la línea 3
        y = (tabla_display[cont_elem_tabla+2].scroll == 1 ? x : 0);
        c3 = tabla_display[cont_elem_tabla+2].mensaje.substring(0+y, long_subcad3 + y);
        // si no quiere scroll en la línea 4
        y = (tabla_display[cont_elem_tabla+3].scroll == 1 ? x : 0);
        c4 = tabla_display[cont_elem_tabla+3].mensaje.substring(0+y, long_subcad4 + y);
        
        imprime_display (tabla_display[cont_elem_tabla].colX1, tabla_display[cont_elem_tabla].fila, c1);
        
        imprime_display (tabla_display[cont_elem_tabla+1].colX1, tabla_display[cont_elem_tabla+1].fila, c2);
        
        imprime_display (tabla_display[cont_elem_tabla+2].colX1, tabla_display[cont_elem_tabla+2].fila, c3);
        
        imprime_display (tabla_display[cont_elem_tabla+3].colX1, tabla_display[cont_elem_tabla+3].fila, c4);             
      
        delay(tabla_display[cont_elem_tabla].delayScroll);
        
        if(salir_bucle > 1){
          borrar_display();   // hemos recibido nuevos datos del brain4it, salimos del bucle
          break;
        }
     }
    cont_elem_tabla +=4;   
    delay(1000);            
  }
  salir_bucle = 1;
}

// imprime en el display posicionando el cursor en las coordenadas dadas
void imprime_display(int columna, int fila, String mensaje) {  
  lcd.setCursor(columna,fila);
  lcd.print(mensaje);
  server.handleClient();   // para detectar si recibimos nuevos datos del brain4it durante la visualización en el display
}

// limpia la pantalla del display
void borrar_display() {
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.setCursor(0,1);
}
