#!/usr/bin/perl
# Elimina llamadas de datagen de las cadenas de Registrate.
# Necesita balanceo de parentesis porque las cadenas son multilinea y los argumentos
# llevan lambdas anidadas: un regex plano corta en el parentesis equivocado.
use strict;
use warnings;

# Metodos que se borran enteros de la cadena, con su lista de argumentos.
my @DROP = qw(
    blockstate blockModel simpleBlock
    partialBaseModel customItemModel standardModel
    customBlockItemModel customGenericItemModel forPowered
);
# .transform(...) solo se borra si el argumento es un helper de datagen;
# el resto de .transform() es real (CStress, FeatureToggle) y se conserva.
my $DATAGEN_ARG = qr/\s*(?:AssetLookup|BlockStateGen|ModelGen|TagGen|SpecialBlockStateGen)\b|\s*customItemModel\s*\(/;

sub strip_call {
    my ($text, $name, $only_if) = @_;
    my $out = '';
    my $i = 0;
    while ($i < length $text) {
        my $idx = index($text, ".$name(", $i);
        if ($idx < 0) { $out .= substr($text, $i); last; }

        my $open = $idx + length(".$name");
        # recorrer hasta el parentesis de cierre correspondiente
        my ($depth, $j, $in_str, $in_chr) = (0, $open, 0, 0);
        while ($j < length $text) {
            my $ch = substr($text, $j, 1);
            my $prev = $j > 0 ? substr($text, $j - 1, 1) : '';
            if ($in_str) { $in_str = 0 if $ch eq '"' && $prev ne "\\"; }
            elsif ($in_chr) { $in_chr = 0 if $ch eq "'" && $prev ne "\\"; }
            elsif ($ch eq '"') { $in_str = 1 }
            elsif ($ch eq "'") { $in_chr = 1 }
            elsif ($ch eq '(') { $depth++ }
            elsif ($ch eq ')') { $depth--; last if $depth == 0 }
            $j++;
        }
        my $args = substr($text, $open + 1, $j - $open - 1);

        if ($only_if && $args !~ /^$only_if/) {
            # no es datagen: conservar tal cual y seguir despues de esta llamada
            $out .= substr($text, $i, $j + 1 - $i);
            $i = $j + 1;
            next;
        }

        $out .= substr($text, $i, $idx - $i);   # todo lo previo
        $i = $j + 1;                            # saltar la llamada entera
        # limpiar el salto de linea y sangria que quedaban colgando
        $out =~ s/\n\s*$/\n/ if substr($text, $i, 1) eq "\n";
    }
    return $out;
}

for my $file (@ARGV) {
    open(my $fh, '<', $file) or die "no se pudo abrir $file: $!";
    local $/; my $src = <$fh>; close $fh;
    my $orig = $src;

    $src = strip_call($src, $_, undef) for @DROP;
    $src = strip_call($src, 'transform', $DATAGEN_ARG);

    # colapsar lineas que quedaron solo con espacios
    $src =~ s/\n[ \t]+\n/\n/g;

    if ($src ne $orig) {
        open(my $out, '>', $file) or die "no se pudo escribir $file: $!";
        print $out $src; close $out;
        print "modificado: $file\n";
    }
}
