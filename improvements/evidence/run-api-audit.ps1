param([string]$BaseUrl='https://veshwik-secure-crypt.embarko.app', [string]$OutputPath="$PSScriptRoot/api-results.json")
$ErrorActionPreference='Stop'
$results=[Collections.Generic.List[object]]::new()
function Call-Crypto($operation,$text,$algorithm,$key){
  $body=@{text=$text;algorithm=$algorithm;key=$key}|ConvertTo-Json -Compress
  $response=Invoke-WebRequest "$BaseUrl/api/$operation" -Method Post -ContentType 'application/json' -Body ([Text.Encoding]::UTF8.GetBytes($body)) -SkipHttpErrorCheck
  return @{status=[int]$response.StatusCode;body=($response.Content|ConvertFrom-Json)}
}
function Record($id,$expected,$actual,$passed){$results.Add([pscustomobject]@{id=$id;expected=$expected;actual=$actual;passed=[bool]$passed})}
$keys=@{CAESAR='3';VIGENERE='LEMON';XOR='audit-key';AES='Audit-only-passphrase-729!';DES='Audit-only-passphrase-729!';TRIPLE_DES='Audit-only-passphrase-729!'}
foreach($algorithm in $keys.Keys|Sort-Object){
  foreach($sample in @(@{name='ascii';text='Attack at dawn! 123'},@{name='unicode';text='नमस्ते café 日本語 🔐'})){
    $enc=Call-Crypto encrypt $sample.text $algorithm $keys[$algorithm]
    $dec=Call-Crypto decrypt $enc.body.result $algorithm $keys[$algorithm]
    Record "$algorithm-$($sample.name)-roundtrip" 'Original text returned' @{encryptStatus=$enc.status;decryptStatus=$dec.status;matches=($dec.body.result -ceq $sample.text);returned=$dec.body.result} ($enc.status -eq 200 -and $dec.status -eq 200 -and $dec.body.result -ceq $sample.text)
  }
}
$known=Call-Crypto encrypt 'ATTACKATDAWN' 'VIGENERE' 'LEMON';Record 'vigenere-known-vector' 'LXFOPVEFRNHR' $known.body.result ($known.body.result -ceq 'LXFOPVEFRNHR')
$caesar=Call-Crypto encrypt 'Abc XYZ' 'CAESAR' '-3';Record 'caesar-negative-shift' 'Xyz UVW' $caesar.body.result ($caesar.body.result -ceq 'Xyz UVW')
foreach($case in @(@{id='empty-text';operation='encrypt';text='';algo='AES';key='test'},@{id='blank-key';operation='encrypt';text='test';algo='AES';key=''},@{id='unknown-algorithm';operation='encrypt';text='test';algo='UNKNOWN';key='test'},@{id='bad-base64';operation='decrypt';text='%%%not-base64%%%';algo='AES';key='test'},@{id='invalid-caesar-key';operation='encrypt';text='test';algo='CAESAR';key='abc'},@{id='invalid-vigenere-key';operation='encrypt';text='test';algo='VIGENERE';key='123'})){
$r=Call-Crypto $case.operation $case.text $case.algo $case.key;Record $case.id 'HTTP 400' @{status=$r.status;message=$r.body.message} ($r.status -eq 400)
}
$enc1=Call-Crypto encrypt 'A long audit sample to verify integrity behavior safely.' 'AES' $keys.AES
$enc2=Call-Crypto encrypt 'A long audit sample to verify integrity behavior safely.' 'AES' $keys.AES
Record 'aes-randomized-output' 'Different ciphertext on repeated encryption' ($enc1.body.result -ne $enc2.body.result) ($enc1.body.result -ne $enc2.body.result)
$wrong=Call-Crypto decrypt $enc1.body.result 'AES' 'wrong-audit-password';Record 'aes-wrong-password' 'HTTP 400' $wrong.status ($wrong.status -eq 400)
$bytes=[Convert]::FromBase64String($enc1.body.result);$bytes[0]=$bytes[0] -bxor 1
$tampered=Call-Crypto decrypt ([Convert]::ToBase64String($bytes)) 'AES' $keys.AES
Record 'aes-tamper-rejection' 'Tampered ciphertext rejected' @{status=$tampered.status;success=$tampered.body.success;plaintext=$tampered.body.result} ($tampered.status -ne 200)
$longText='A'*20000;$large=Call-Crypto encrypt $longText 'AES' $keys.AES;$largeDec=Call-Crypto decrypt $large.body.result 'AES' $keys.AES
Record 'aes-20000-ascii-roundtrip' 'Accepted encryption output can be decrypted' @{encryptStatus=$large.status;ciphertextLength=$large.body.result.Length;decryptStatus=$largeDec.status;message=$largeDec.body.message} ($large.status -eq 200 -and $largeDec.status -eq 200 -and $largeDec.body.result -eq $longText)
$tooLong=Call-Crypto encrypt ('A'*20001) 'AES' $keys.AES;Record 'reject-20001-input' 'HTTP 400' $tooLong.status ($tooLong.status -eq 400)
foreach($length in @(16,32)){ $r=Invoke-RestMethod "$BaseUrl/api/generate-key?length=$length";Record "key-generator-$length" "$length characters" $r.result.Length ($r.result.Length -eq $length)}
$weak=Invoke-RestMethod "$BaseUrl/api/key-strength?key=Password123!";Record 'predictable-password-rating' 'A common predictable password is not labeled STRONG' $weak.result ($weak.result -ne 'STRONG')
$pageResponse=Invoke-WebRequest $BaseUrl
$headers=@{};foreach($name in @('Content-Security-Policy','Strict-Transport-Security','X-Content-Type-Options','Referrer-Policy','Cache-Control')){$headers[$name]=$pageResponse.Headers[$name] -join ', '}
$out=@{testedAtUtc=[DateTime]::UtcNow.ToString('o');baseUrl=$BaseUrl;results=@($results.ToArray());responseHeaders=$headers;summary=@{total=$results.Count;passed=@($results|Where-Object passed).Count;failed=@($results|Where-Object {-not $_.passed}).Count}}
$out|ConvertTo-Json -Depth 10|Set-Content $OutputPath
$out.summary|ConvertTo-Json
$results|Where-Object {-not $_.passed}|ConvertTo-Json -Depth 6
