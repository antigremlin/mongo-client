package org.mongodb.codec;

import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"org.mongodb.codec.Mongodb.Codec"})
public class CodecProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getRootElements()) {
            System.out.println("spike.Processor: root element: " + element);
        }
        for (TypeElement annotation : annotations) {
            System.out.println("spike.Processor: annotation: " + annotation);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Mongodb.Codec.class)) {
            processTemplate(element);
        }
        return true;
    }

    private void processTemplate(Element element) {
        try {
            PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(element);
            String className = packageElement.getQualifiedName().toString() + "." + element.getSimpleName() + "_codec";
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(className);
            try (Writer w = sourceFile.openWriter()) {
                STGroupFile stg = new STGroupFile("templates/codec.stg");
                ST tpl = stg.getInstanceOf("codec");
                tpl.add("package", packageElement);
                tpl.add("element", element);
                AutoIndentWriter autoIndentWriter = new AutoIndentWriter(w);
                tpl.write(autoIndentWriter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
        }
    }
}
