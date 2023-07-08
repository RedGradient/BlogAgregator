@import java.util.Map

@param Map<String, String> channels

<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="public/styles.css">
    <link rel="stylesheet" href="/css/stripes.css">
    <title>Feed</title>
</head>
<body>
    <ol>
        @for(var channel : channels.entrySet())
            <li><a href="${channel.getValue()}">${channel.getKey()}</a></li>
        @endfor
    </ol>
</body>
</html>
