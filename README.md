# Uruchamianie scrapera

pobieramy plik /parser/targer/parser-1.0-SNAPSHOT-jar-with-dependencies.jar
uruchamiamy go komendą:
java -jar parser-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 $3
gdzie
$1 = adres api sklepu
$2 = klucz api z odpowiednimi uprawnieniami
$3 = liczba stron do pobrania z katalogu zalando(1 strona ~= 24 produkty)

przykład:
java -jar parser-1.0-SNAPSHOT-jar-with-dependencies.jar http://localhost:8080/api IJP82WEE6LDEBZ7BJXYZQWLVSUZJLBUH 10
