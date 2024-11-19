package com.alurachallenge.cursos.literatura.service;

import com.alurachallenge.cursos.literatura.api.PeticionAPI;
import com.alurachallenge.cursos.literatura.model.Autor;
import com.alurachallenge.cursos.literatura.model.Idioma;
import com.alurachallenge.cursos.literatura.model.Libro;
import com.alurachallenge.cursos.literatura.model.LibroRecord;
import com.alurachallenge.cursos.literatura.util.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MenuService {
    private PeticionAPI peticionAPI;
    private Scanner sc;
    private LibroService libroService;
    private AutorService autorService;
    private JsonParser jsonParser;

    @Autowired
    public MenuService(LibroService libroService, AutorService autorService, JsonParser jsonParser) {
        this.peticionAPI = new PeticionAPI();
        this.sc = new Scanner(System.in);
        this.libroService = libroService;
        this.autorService = autorService;
        this.jsonParser = jsonParser;
    }

    public void guardarLibro() {
        List<LibroRecord> librosObtenidos = obtenerLibrosApi();

        if (librosObtenidos.isEmpty()) {
            System.out.println("No se encontró ningún libro");
            return;
        }

        System.out.println("Seleccione un libro para guardar[0-Cancelar]");
        for (int i = 0; i < librosObtenidos.size(); i++) {
            System.out.println((i + 1) + " - " + librosObtenidos.get(i).titulo() + " - " + librosObtenidos.get(i).idiomas().get(0) + " - " + librosObtenidos.get(i).autores().get(0).nombre());
        }

        int opcion = sc.nextInt();
        sc.nextLine();
        if (opcion == 0) {
            return;
        }
        if (opcion < 1 || opcion > librosObtenidos.size()) {
            System.out.println("Error");
            return;
        }

        LibroRecord libroRecord = librosObtenidos.get(opcion - 1);
        Optional<Libro> libroTraidoDelRepo = libroService.obtenerLibroPorNombre(libroRecord.titulo());
        Optional<Autor> autorTraidodelRepo = autorService.obtenerAutorPorNombre(libroRecord.autores().get(0).nombre());

        if (libroTraidoDelRepo.isPresent()) {
            System.out.println("El libro ya cuenta con un registro");
            return;
        }

        Libro libro = new Libro(libroRecord);

        if (!autorTraidodelRepo.isPresent()) {
            Autor autorNuevo = libro.getAutor();
            autorService.guardarAutor(autorNuevo);
        }

        libroService.guardarLibro(libro);
    }

    public List<LibroRecord> obtenerLibrosApi() {
        System.out.print("Ingrese el título del libro [0-Cancelar]: ");
        String titulo = sc.nextLine();
        if (titulo.equals("0")) {
            return Collections.emptyList();
        }
        List<LibroRecord> librosObtenidos;
        librosObtenidos = jsonParser.parsearLibros(peticionAPI.obtenerDatos(titulo));
        return librosObtenidos;
    }


    public void listarLibrosRegistrados() {
        List<Libro> libros = libroService.obtenerTodosLosLibros();
        libros.forEach(libro -> libro.imprimirInformacion());
    }

    public void listarAutoresRegistrados() {
        List<Autor> autores = autorService.obtenerTodosLosAutores();
        autores.forEach(autor -> autor.imprimirInformacion());
    }

    public void listarAutoresVivosEnAnio() {
        try {
            System.out.print("Ingrese año: ");
            int anio = sc.nextInt();
            sc.nextLine();
            List<Autor> autores = autorService.obtenerAutoresVivosEnAnio(anio);
            autores.forEach(autor -> autor.imprimirInformacion());
        } catch (InputMismatchException e) {
            System.out.println("Error, vuelva a ingresar un número");
        }

    }

    public void listarLibrosPorIdioma() {
        Idioma.listarIdiomas();
        System.out.print("Ingrese el codigo del idioma [0-Cancelar]: ");
        String idiomaBuscado = sc.nextLine();
        if (idiomaBuscado.equals("0")) {
            return;
        }
        List<Libro> libros = libroService.obtenerLibrosPorIdioma(Idioma.stringToEnum(idiomaBuscado));
        libros.forEach(libro -> libro.imprimirInformacion());
    }

}
