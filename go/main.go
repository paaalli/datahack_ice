package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"

	"github.com/gorilla/mux"
)

var countryList []string

func initCountryList() {
	r, err := http.NewRequest("GET", "https://restcountries.eu/rest/v2/all", nil)
	if err != nil {
		log.Fatal("errrr")
	}
	resp, err := http.DefaultClient.Do(r)
	if err != nil {
		log.Fatal("err")
	}

	type country struct {
		Name string `json:"name"`
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	var d []country
	err = json.Unmarshal(body, &d)
	if err != nil {
		log.Fatal("err")
	}
	for _, c := range d {
		countryList = append(countryList, c.Name)
	}
}

func main() {
	initCountryList()

	r := mux.NewRouter()
	r.HandleFunc("/countries", handleCountries).Methods("GET")
	r.HandleFunc("/vaccination/{country}", handleCountryVaccination).Methods("GET")
	r.HandleFunc("/prescription/{country}", handlePrescriptionLimitation).Methods("GET")
	r.HandleFunc("/interactions", handlePrescriptionInteraction).Methods("GET")
	http.Handle("/", r)

	err := http.ListenAndServe(":1234", nil)
	if err != nil {
		fmt.Printf("Couldnt start server: %q\n", err)
	}
}

func handleCountries(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Access-Control-Allow-Origin", "*")

	err := json.NewEncoder(w).Encode(countryList)
	if err != nil {
		log.Println(err)
	}
}

func handleCountryVaccination(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Access-Control-Allow-Origin", "*")

	vars := mux.Vars(r)
	c := vars["country"]

	// Waiting for authentication token from API source. Expected to receive it but ended up having to mock.
	vaccineData := map[string][]string{
		"Germany": []string{"Hepatitus A"},
		"Brazil":  []string{"hepatitis b", "yellow fever"},
	}

	err := json.NewEncoder(w).Encode(vaccineData[c])
	if err != nil {
		log.Println(err)
	}
}

func handlePrescriptionLimitation(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Access-Control-Allow-Origin", "*")

	vars := mux.Vars(r)
	c := vars["country"]

	// Real data, would have to be manually filled in I'm afraid.
	prescriptionDuration := map[string]int{
		"Brazil":         30,
		"Germany":        21,
		"Czech Republic": 14,
	}
	fmt.Println(c)
	err := json.NewEncoder(w).Encode(prescriptionDuration[strings.Replace(c, "+", " ", -1)])
	if err != nil {
		log.Println(err)
	}
}

func handlePrescriptionInteraction(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Access-Control-Allow-Origin", "*")

	err := r.ParseForm()
	if err != nil {
		log.Println(err)
		return
	}
	names := r.Form["name"]
	var rxcuis []string
	for _, n := range names {
		r, err := http.NewRequest("GET", "https://rxnav.nlm.nih.gov/REST/drugs?name="+n, nil)
		if err != nil {
			log.Println(err)
			return
		}
		resp, err := http.DefaultClient.Do(r)
		if err != nil {
			log.Println("err")
			return
		}

		defer resp.Body.Close()
		body, err := ioutil.ReadAll(resp.Body)
		fmt.Println(string(body))
		i := strings.Index(string(body), "rxcui>")
		fmt.Println(i)
		if i > len(string(body)) || i < 0 {
			continue
		}
		j := strings.Index(string(body)[i:], "<")
		fmt.Println(j)
		if j > len(string(body)) || j < 0 {
			continue
		}
		rxcui := string(body)[i+6 : j+i]
		rxcuis = append(rxcuis, rxcui)
	}
	r, err = http.NewRequest("GET", "https://rxnav.nlm.nih.gov/REST/interaction/list.json?rxcuis="+strings.Join(rxcuis, "+"), nil)
	if err != nil {
		log.Println(err)
		return
	}
	resp, err := http.DefaultClient.Do(r)
	if err != nil {
		log.Println(err)
		return
	}

	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Println(err)
		return
	}

	type data struct {
		F []struct {
			T []struct {
				P []struct {
					Severity    string `json:"severity"`
					Description string `json:"description"`
				} `json:"interactionPair"`
			} `json:"fullInteractionType"`
		} `json:"fullInteractionTypeGroup"`
	}
	var d data
	err = json.Unmarshal(body, &d)
	if err != nil {
		log.Println(err)
		return
	}

	type response struct {
		Severity    string `json:"severity"`
		Description string `json:"description"`
	}
	dr := []response{}
	for _, f := range d.F {
		for _, t := range f.T {
			for _, p := range t.P {
				dr = append(dr, response{p.Severity, p.Description})
			}
		}
	}
	err = json.NewEncoder(w).Encode(dr)
	if err != nil {
		log.Println(err)
	}
}
