#!/usr/bin/perl
# Benötigte Software
# - Strawberry Perl strawberry-perl-5.32.1.1-64bit
# - Perl Modul Image::ExifTool
# 	- CPAN Client von Strawberry perl öffen und "install Image::ExifTool" eingeben
# ImageMagick http://www.imagemagick.org/script/download.php (ImageMagick-7.1.0-35-Q16-HDRI-x64-dll.exe)
#	- Installieren mit 
#		- "Install Development Headers and libraries for C and C++"
#		- "Install PerlMagick for Strawberry Perl v5.30"
# 	- CPAN Client von Strawberry perl öffen und "install Image::Magick" eingeben
# Schrift "FreeSansBold.ttf" installieren (Achtung, für ALLE User Installieren, sonst landet sie im User Verzeichnis)
# Verküpfung unter C:\Users\indy\AppData\Roaming\Microsoft\Windows\SendTo einrichten

use strict;
use warnings;
use Encode qw/from_to/;
use Image::Magick;
use Image::ExifTool;

#Globale Variablen
my $targetWidth = 1024; 
my $targetHeight = 768;
my $minWidth = 550;
my $minHeight = 550;
my $targetDir = 'W:\Boris\Fotorahmen';


my $targetRatio = $targetWidth / $targetHeight;

#Steuerung der Forks
my $maxForks = 4;
my $currentForks = 0;

my $sourceDir = $ARGV[0];

my $image;
my $cropSizeVertical;

### Begin
if ($sourceDir eq '') {
	print "Usage: scalePhotos PicDir\n";
	exit 0;
}

print "Starting scalePhotosWin.pl ...\r\n";
my @pics = readPics($sourceDir);
my $pic_number = scalar @pics;
my $pic_counter = 1;
print "$pic_number images found in \"$sourceDir\"\r\n";

foreach my $pic (@pics){
	
	#Wir warten erstmal mit dem forken, falls wie zu viele Prozesse haben
	#while ($currentForks >= $maxForks){
	#	wait;
	#	$currentForks--;
	#}

	#OK, wir forken weiter
	#if (my $pid = fork){
		#Elternprozess
		#print "Child forked, PID ist $pid\n";
	#	$currentForks++;

	#}
	#else{
		#Child
		#Prozessiere das Bild und beende Dich danach!
		
		my $pic_counter_disp = $pic_counter;
		while (length $pic_counter_disp < length $pic_number) {
			$pic_counter_disp = "0" . $pic_counter_disp;
		}
		
		print "Processing $pic_counter_disp of $pic_number --- ";
		$image = Image::Magick->new;
		$image->Read(Win32::GetShortPathName($pic));
		$image = autoOrientPic($image);
		if (shouldWeProcessThisImage($image)) {
			$image = scaleImage($image);
			$image = commentImage($image, $pic);
			my $createdDir = createTargetDir($pic);
			#from_to($createdDir,"windows-1252","cp850", Encode::FB_QUIET);
			my $picname = getPicName($pic);
			$image->Write("$createdDir\\$picname");
			print "OK, saved to: \"$createdDir\\$picname\"\r\n";
		}
		else {
			print "Skipped \"$pic\"\r\n";
		}
		@$image = ();
		$pic_counter++;
		#exit 0;
	#}
}
print "Press Any Key to Continue . . .";
getc(STDIN); 

sub createTargetDir {
	
	my $pic = $_[0];
	
	#Ersetze den ganzen Pfad durch das letzte Unterverzeichnis
	my $specTargetDir = getLastSubdir($pic);
	my $createdDir = $targetDir . "\\" . $specTargetDir . "_win";
	if (!-e $createdDir){
		mkdir "$createdDir" or die "Konnte Verzeichnis \"$createdDir\" nicht anlegen.";
	}
	
	return $createdDir
}

sub readPics {

	my 	@pics;
	
	if ( $sourceDir =~ /.*(jpg|jpeg)$/i ) {
		$pics[0] = $sourceDir;
	}
	else {
		my $searchPath = $sourceDir . "\\\*.jpg";
		@pics = glob "'$searchPath'";
		$searchPath = $sourceDir . "\\\*.jpeg";
		push @pics, glob "'$searchPath'";
	}
	chomp @pics;

	return @pics
}

sub autoOrientPic {

	my $orientImage = $_[0];

	$orientImage->AutoOrient();
	$orientImage->Set(orientation=>'top-left');

	return $orientImage;

}

sub shouldWeProcessThisImage {

	my $testImage = $_[0];

	my $origHeight = $testImage->Get('height');
	my $origWidth = $testImage->Get('width');
	my $origRatio = $origWidth / $origHeight;

	return 0 if $origHeight <= $minHeight;	#Bild ist zu klein
	return 0 if $origWidth <= $minWidth;	#Bild ist zu schmal
	#return 0 if $origRatio > 2;		#Ist wohl ein Panorama

	return 1;
}


sub scaleImage {

	my $scaleImage = $_[0];
	
	my $origHeight = $scaleImage->Get('height');
	my $origWidth = $scaleImage->Get('width');
	my $origRatio = $origWidth / $origHeight;
	my $cropSizeY;
	my $cropSizeX;

	if ($origRatio < $targetRatio and $origRatio > 1) { 
		#Das Bild hat ein kleineres Seitenverhältnis als das Ziel, ist aber Landscape
		#Die maximale Ausdehnung wird also von der Breite angegeben
		#Überstehende Höhe wird vermittelt und abgeschnitten
		$scaleImage->Resize(geometry=>$targetWidth . 'x' . $targetWidth / $origRatio);
		$cropSizeY = (($targetWidth / $origRatio) - $targetHeight ) / 2; 
		$scaleImage->Crop(height=>$targetHeight, y=>$cropSizeY );
	}
	elsif ($origRatio > $targetRatio and $origRatio > 1) { 
		#Das Bild hat ein größeres Seitenverhältnis als das Ziel und ist Landscape
		#Die maximale Ausdehnung wird also von der Höhe angegeben
		#Überstehende Breite wird vermittelt und abgeschnitten
		$scaleImage->Resize(geometry=>$targetHeight * $origRatio. 'x' . $targetHeight);
		$cropSizeX = (($targetHeight  * $origRatio) - $targetWidth) / 2; 
		$scaleImage->Crop(width=>$targetWidth, x=>$cropSizeX );
	}
	elsif ($origRatio = $targetRatio and $origRatio > 1) { 
		#Das Bild hat ein gleiches Seitenverhältnis als das Ziel und ist Landscape
		$scaleImage->Resize(geometry=>$targetWidth . 'x' . $targetWidth / $origRatio);
	}
	elsif ($origRatio < 1) { 
		#Das Bild ist Portrait
		#Die maximale Ausdehnung wird also von der Breite angegeben
		$scaleImage->Resize(geometry=>$targetHeight / $origRatio . 'x' . $targetHeight);

	}

	$cropSizeVertical = $cropSizeY;


	return $scaleImage;
}

sub commentImage{

	my $commImage = $_[0];
	my $picture = $_[1];
	my %annotation = (
		#font        => 'C:\Windows\Fonts\GIGI.TTF',
		font        => 'C:\Windows\Fonts\FreeSansBold.ttf',
		#font        => '/usr/share/fonts/truetype/freefont/FreeSansBold.ttf',
		fill        => 'black',
		#undercolor  => 'grey',	#Orignal
		x           => 0,
		y           => 4,	#Unter Linux "10", beschreibt die Höhe der gesamten Annotation
		pointsize   => 30,	#Original 15
		text        => '',
		family      => '',
		style       => 'Normal',
		weight      => 1,
		density     => '',
		#stroke      => 'white',	#Original auskommentiert
		#strokewidth => 1,	#Original auskommentiert
		kerning     => 0,
		geometry    => '',
		affine      => '',
		translate   => 0,
		scale       => 1,
		rotate      => 0,
		skewX       => 0,	#Anschrägen X
		skewY       => 0,	#Anschrägen Y
		#align       => 'Left',
		encoding    => '',
		stretch     => 'Normal',
		gravity     => 'South',
		antialias   => 'true'
	);
	
	#Ergänzen des %annotation um den den Comment-String
	%annotation = %{createCommentString($commImage, \%annotation, $picture)};

	#Hintergrund erstellen
	$commImage = createTextBg($commImage, \%annotation);
			
	#Der Crop ist buggy
	#Wir müssen den Textversatz manuell ausrechnen wenn wir croppen...
	#Das pass jedenfalls für eine Höhe von 480 ;-)
	$annotation{y} += 2 * $cropSizeVertical + 30 if $cropSizeVertical;
	undef $cropSizeVertical;

	#Schreiben der Anmerkung auf das Bild
	$commImage->Annotate(%annotation);

	return $commImage;
}

sub createCommentString{

	my $commImage = $_[0];
	my %annotation = %{$_[1]}; 
	my $picture =  $_[2];
	my $comment = $commImage->Get('comment');
				
	# Create a new Image::ExifTool object
	my $exifTool = new Image::ExifTool;
 
	# Extract meta information from an image
	$exifTool->ExtractInfo($picture);
	my $exifDate = $exifTool->GetValue('DateTimeOriginal');
	if (defined $exifDate && $exifDate ne '') {
		chomp $exifDate;
		$exifDate =~ s/.*([\d]{4}):([\d]{2}):([\d]{2}).*/$3.$2.$1/;
	} else {
		$exifDate = (stat $picture)[9];
		my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime($exifDate);
		$year = $year + 1900;
		$mday = "0". $mday if (length $mday == 1);
		$mon = "0". $mon if (length $mon == 1);
		$exifDate = $mday . "." . $mon . "." . $year; 
		print "No EXIF Date, using File CDate --- ";
	}
	
	#Ersetze den ganzen Pfad durch das letzte Unterverzeichnis
	my $dir = getLastSubdir($picture);
	
	#Entferne vorangestelltes Datum aus dem Unterverzeichnis, falls vorhanden.
	$dir = $1 if $dir =~ /^[\d\-\s]{10,11}(.*)/;
	chomp($dir);

	$annotation{text} = $dir;
	$annotation{text} = $annotation{text} . " - " . $comment if defined $comment ;
	$annotation{text} = $annotation{text} . " am " . $exifDate if $exifDate ne '';

	#Testen, ob der Titel auf das Bild passt...
	if (textDoesNotFit($commImage, \%annotation)){
		if (defined $comment){
			$annotation{text} = $comment;
		}
		else{
			$annotation{text} = $dir;
		}
		$annotation{text} = $annotation{text} . " am " . $exifDate if $exifDate ne '';
	}
	if (textDoesNotFit($commImage, \%annotation)){
		#Weglassen des Verzeichnisses und des Datums
		if (defined $comment){
			$annotation{text} = $comment;
		}
		else{
			$annotation{text} = $dir;
		}
	}
	#Kürzen des Kommentars, wenn es immernoch nicht reicht!
	while (textDoesNotFit($commImage, \%annotation)){
		$annotation{text} =~ s/(.*)\s.*/$1/;
		$annotation{text} = $annotation{text} . "...";
	}

	return \%annotation;
}

sub textDoesNotFit {

	my $testImage = $_[0];
	my %annotation = %{$_[1]};
	my %fontMetrics;
	
	%fontMetrics = %{getFontMetrics($testImage, \%annotation)};

	
	if ($fontMetrics{width} > $testImage->Get('width') - 20){
		return 1;
	}
	else{
		return 0;
	}
}

sub getFontMetrics{

	my $testImage = $_[0];
	my %annotation = %{$_[1]};
	my %fontMetrics;

	($fontMetrics{x_ppem}, 
	 $fontMetrics{y_ppem}, 
	 $fontMetrics{ascender}, 
	 $fontMetrics{descender}, 
	 $fontMetrics{width}, 
	 $fontMetrics{height}, 
	 $fontMetrics{max_advance}) = $testImage->QueryFontMetrics(%annotation);

	#Korrekturfaktor für die Höhe eingebaut, damit es mit der Linux Daten passt
	$fontMetrics{height} = $fontMetrics{height} * 0.75;
	
	#print("\nx_ppem: $x_ppem\ny_ppem: $y_ppem\nascender: $ascender\ndescender: $descender\nwidth: $width\nheight: $height\nmax_advance: $max_advance\n");

	return \%fontMetrics
}

sub createTextBg {

	my $image = $_[0];
	my %annotation = %{$_[1]};
	my ($x1, $y1, $x2, $y2);
	my %fontMetrics = %{getFontMetrics($image, \%annotation)};

	my $imageBg = Image::Magick->new;
	my $imageMask = Image::Magick->new;

	#Komplett weißen Hintergrund in Bildgröße erstellen, der hinterher durchscheint
	$imageBg->Set(size=>$image->Get('width') . 'x' . $image->Get('height'));
	$imageBg->Read("xc:white");
	
	#Erstellen der Maske in schwarz (intransparent)
	$imageMask->Set(size=>$image->Get('width') . 'x' . $image->Get('height'));
	$imageMask->Read("xc:black");

	#Wo soll das graue Rechteck auf die Maske (Transparenz)?
	$x1 = 0;
	#Korrekturfaktor für die Höhe eingebaut, damit es mit der Linux Daten passt (letzer Multiplikator) <-- Bestimmt die Höhe der Schrift im Bild
	$y1 = $imageMask->Get('height') - $annotation{y} - $fontMetrics{height} - $fontMetrics{height}*.15;
	$x2 = $imageMask->Get('width') - $x1;
	$y2 = $y1 + $fontMetrics{height};
	$imageMask->Draw(primitive=>'rectangle', points=>"$x1,$y1,$x2,$y2", fill=>'#888888');
	
	#Jetzt noch der text in Weiß auf die Maske, damit er voll durchscheint (keine Transparenz!)

	$annotation{fill} = 'white';
	$imageMask->Annotate(%annotation);
	
	#$imageMask->Display;
	$image->Composite(image=>$imageBg, mask=>$imageMask);

	return $image;
}

sub getLastSubdir {

	my $pic = $_[0];

	$pic =~ s/.*\\(.*)\\.*\.(jpg|jpeg)$/$1/i;
	
	return $pic
}

sub getPicName {

	my $pic = $_[0];

	$pic =~ s/.*\\(.*\.(jpg|jpeg))$/$1/i;
	$pic =~ s/ä/ae/g;
	$pic =~ s/Ä/Ae/g;
	$pic =~ s/ö/oe/g;
	$pic =~ s/Ö/Oe/g;
	$pic =~ s/ü/ue/g;
	$pic =~ s/Ü/Ue/g;
	$pic =~ s/ß/ss/g;
	
	return $pic
}


